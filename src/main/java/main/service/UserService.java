package main.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Random;
import java.util.UUID;
import java.util.stream.Collectors;
import javax.imageio.ImageIO;
import javax.servlet.http.HttpServletRequest;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import main.dto.LoginDto;
import main.dto.RegisterForm;
import main.dto.ResponsePostApi;
import main.dto.UserDto;
import main.mapper.CommentMapper;
import main.mapper.PostMapper;
import main.model.CaptchaCode;
import main.model.GlobalSettings;
import main.model.Post;
import main.model.Role;
import main.model.User;
import main.repository.CaptchaCodeRepository;
import main.repository.GlobalSettingsRepository;
import main.repository.PostRepository;
import main.repository.UserRepository;
import org.imgscalr.Scalr;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Service
@Slf4j
public class UserService implements UserDetailsService {

  private static final Map<String, Integer> authorizedUsers = new HashMap<>();

  private static final String SHORT_PASSWORD = "Пароль короче 6-ти символов";

  private static final String WRONG_CAPTCHA = "Код с картинки введён неверно";

  @Autowired
  UserRepository userRepository;

  @Autowired
  GlobalSettingsRepository globalSettingsRepository;

  @Autowired
  CommentMapper commentMapper;

  @Autowired
  PostMapper postMapper;

  @Autowired
  CaptchaCodeRepository captchaCodeRepository;

  @Autowired
  HttpServletRequest request;

  @Autowired
  private MailSender mailSender;

  @Autowired
  PostRepository postRepository;

  @Autowired
  AuthenticationService authenticationService;

  public static Map<String, Integer> getAuthorizedUsers() {
    return authorizedUsers;
  }

  @Override
  public UserDetails loadUserByUsername(@NonNull String username)
      throws UsernameNotFoundException {
    User user = userRepository.findByEmail(username)
        .orElseThrow(() -> new UsernameNotFoundException("user " + username + " not found!"));
    return new org.springframework.security.core.userdetails.User(user.getEmail(),
        user.getPassword()
        , new ArrayList<>());
  }

  @Bean
  public PasswordEncoder passwordEncoder() {
    return new BCryptPasswordEncoder();
  }

  public JsonNode saveUser(RegisterForm registerFormUser) {
    ObjectMapper mapper = new ObjectMapper();
    ObjectNode object = mapper.createObjectNode();
    ObjectNode objectError = mapper.createObjectNode();

    Optional<User> byEmail = userRepository.findByEmail(registerFormUser.getE_mail());
    if (byEmail.isEmpty() && registerFormUser.getPassword().length() > 5
        && registerFormUser.getName().length() > 0 && registerFormUser.getName().length() < 1000) {
      User user = new User();
      user.setEmail(registerFormUser.getE_mail());
      user.setName(registerFormUser.getName());
      user.setRole(new Role(1, "ROLE_USER"));
      user.setRegTime(LocalDateTime.now());
      user.setPassword(passwordEncoder().encode(registerFormUser.getPassword()));
      user.setCode(UUID.randomUUID().toString());
      userRepository.save(user);
      object.put("result", true);
    } else {
      object.put("result", false);
      if (byEmail.isPresent()) {
        objectError.put("email", "Этот e-mail уже зарегистрирован");
      }
      if (registerFormUser.getPassword().length() < 6) {
        objectError.put("password", SHORT_PASSWORD);
      }
      if (registerFormUser.getName().length() < 1 || registerFormUser.getName().length() > 1000) {
        objectError.put("name", "Имя указано неверно");
      }
      object.put("errors", objectError);
    }

    return object;
  }

  public JsonNode login(LoginDto loginDto) {
    ObjectMapper mapper = new ObjectMapper();
    ObjectNode object = mapper.createObjectNode();
    Optional<User> userByEmail = userRepository.findByEmail(loginDto.getE_mail());
    if (userByEmail.isPresent() && passwordEncoder()
        .matches(loginDto.getPassword(), userByEmail.get().getPassword())) {
      String sessionId = request.getSession().getId();
      User currentUser = userByEmail.get();
      authorizedUsers.put(sessionId, currentUser.getId());
      int allPosts = (int) postRepository.count();
      int moderationCount = postRepository.findAllPostsToModeration(0,
          allPosts, "NEW").size();
      UserDto userDto = UserDto.builder()
          .id(currentUser.getId())
          .name(currentUser.getName())
          .photo(currentUser.getPhoto())
          .email(currentUser.getEmail())
          .moderation(true)
          .moderationCount(moderationCount)
          .settings(true)
          .build();

      ObjectNode objectUser = mapper.valueToTree(userDto);

      object.put("result", true);
      object.put("user", objectUser);
    } else {
      object.put("result", false);
    }
    return object;
  }

  public JsonNode check() {
    ObjectMapper mapper = new ObjectMapper();
    ObjectNode object = mapper.createObjectNode();
    String sessionId = request.getSession().getId();
    if (authorizedUsers.containsKey(sessionId)) {
      Integer id = authorizedUsers.get(sessionId);
      User user = userRepository.findById(id).get();
      UserDto userDto;
      if (user.getIsModerator() == 1) {
        int allPosts = (int) postRepository.count();
        int moderationCount = postRepository.findAllPostsToModeration(0,
            allPosts, "NEW").size();
        userDto = UserDto.builder()
            .id(id)
            .name(user.getName())
            .photo(user.getPhoto())
            .email(user.getEmail())
            .moderation(true)
            .moderationCount(moderationCount)
            .settings(true)
            .build();
      } else {
        userDto = UserDto.builder()
            .id(id)
            .name(user.getName())
            .photo(user.getPhoto())
            .email(user.getEmail())
            .moderation(false)
            .moderationCount(0)
            .settings(true)
            .build();
      }

      ObjectNode objectUser = mapper.valueToTree(userDto);

      object.put("result", true);
      object.put("user", objectUser);
    } else {
      object.put("result", false);
    }
    return object;
  }

  public JsonNode restore(String email) {
    ObjectMapper mapper = new ObjectMapper();
    ObjectNode object = mapper.createObjectNode();
    Optional<User> userByEmail = userRepository.findByEmail(email);
    if (userByEmail.isPresent()) {
      mailSender.send(email, "Code", "/login/change-password/" + userByEmail.get().getCode());
      object.put("result", true);
      return object;
    } else {
      object.put("result", false);
      return object;
    }
  }

  @Transactional
  public JsonNode postNewPassword(String code, String password, String captcha,
      String captcha_secret) {
    ObjectMapper mapper = new ObjectMapper();
    ObjectNode object = mapper.createObjectNode();
    ObjectNode objectError = mapper.createObjectNode();
    Optional<User> userByCode = userRepository.findByCode(code);
    Optional<CaptchaCode> captchaCode = captchaCodeRepository.findByCode(captcha);
    if (userByCode.isPresent() && captchaCode.isPresent() && password.length() > 5
        && captchaCode.get().getSecretCode().equals(captcha_secret)) {
      User user = userRepository.getOne(userByCode.get().getId());
      user.setPassword(passwordEncoder().encode(password));

      object.put("result", true);
    } else {
      object.put("result", false);
      if (password.length() < 6) {
        objectError.put("password", SHORT_PASSWORD);
      }
      if (captchaCode.isPresent() && !captchaCode.get().getSecretCode().equals(captcha_secret)
          || captchaCode.isEmpty()) {
        objectError.put("captcha", WRONG_CAPTCHA);
      }
      if (userByCode.isEmpty()) {
        objectError.put("code", "Ссылка для восстановления пароля устарела."
            + "     <a href=     \"/auth/restore\">Запросить ссылку снова</a>");
      }
      object.put("errors", objectError);
    }
    return object;
  }

  @Transactional
  public JsonNode postNewProfile(MultipartFile photo, String name, String email,
      String password, Integer removePhoto)
      throws Exception {
    ObjectMapper mapper = new ObjectMapper();
    ObjectNode object = mapper.createObjectNode();
    ObjectNode objectError = mapper.createObjectNode();
    User user = authenticationService.getCurrentUser();
    Optional<User> userByEmail = userRepository.findByEmail(email);
    if (userByEmail.isEmpty() && name != null
        && name.length() > 0 &&
        name.length() < 1000 && email != null &&
        password == null && photo == null &&
        removePhoto == null) {
      User userToUpdate = userRepository.getOne(user.getId());
      userToUpdate.setName(name);
      userToUpdate.setEmail(email);
      object.put("result", true);
    }
    if (name != null && name.length() > 0 &&
        name.length() < 1000 && email != null &&
        password != null && photo == null &&
        removePhoto == null) {
      User userToUpdate = userRepository.getOne(user.getId());
      userToUpdate.setPassword(passwordEncoder().encode(password));
      object.put("result", true);
    }
    if (name != null && name.length() > 0 &&
        name.length() < 1000 && email != null &&
        password == null && photo != null &&
        removePhoto == 1) {
      User userToUpdate = userRepository.getOne(user.getId());
      userToUpdate.setPhoto(null);
      object.put("result", true);
    }
    if (name != null && name.length() > 0 &&
        name.length() < 1000 && email != null &&
        password != null && photo != null &&
        removePhoto != null) {
      User userToUpdate = userRepository.getOne(user.getId());
      String fileName = uploadImageWithResize(photo, userToUpdate.getId());
      File file = new File(fileName);
      userToUpdate.setPhoto("/api/image/" + file.getName());
      userToUpdate.setPassword(passwordEncoder().encode(password));
      object.put("result", true);
    }
    if (userByEmail.isPresent() && !user.getEmail().equals(userByEmail.get().getEmail())
        || name.length() < 1
        || name.length() > 1000 || password.length() < 6) {
      object.put("result", false);
      if (userByEmail.isPresent() && !user.getEmail().equals(userByEmail.get().getEmail())) {
        objectError.put("email", "Этот e-mail уже зарегистрирован");
      }
      if (name.length() < 1
          || name.length() > 1000) {
        objectError.put("name", "Имя указано неверно");
      }
      if (password.length() < 6) {
        objectError.put("password", "Пароль короче 6-ти символов");
      }
      object.put("errors", objectError);
    }
    return object;
  }

  private String uploadImageWithResize(MultipartFile image, int userId) throws IOException {
    byte[] byteArr = image.getBytes();
    InputStream inputStream = new ByteArrayInputStream(byteArr);
    BufferedImage bufferedImage = ImageIO.read(inputStream);
    BufferedImage scaledImage = Scalr.resize(bufferedImage, 100);
    File dir = new File("src/main/resources/static/avatars");
    if (!dir.exists()) {
      dir.mkdir();
    }
    File filePath = new File(dir + "/" + userId + ".jpg");
    ImageIO.write(scaledImage, "jpg", filePath);
    return filePath.toString();
  }

  public JsonNode getMyStatistics() throws Exception {
    User currentUser = authenticationService.getCurrentUser();
    ;
    ObjectMapper mapper = new ObjectMapper();
    ObjectNode object = mapper.createObjectNode();
    List<Post> myPosts = postRepository.findAllMyPosts(0, (int) postRepository.count(),
        "ACCEPTED", currentUser.getId());
    List<ResponsePostApi> postList = myPosts.stream().map(p -> postMapper.postToResponsePostApi(p))
        .collect(Collectors.toList());
    object.put("postsCount", myPosts.size());

    int likesCount = postList.stream().mapToInt(p -> p.getLikeCount()).sum();
    object.put("likesCount", likesCount);

    int dislikesCount = postList.stream().mapToInt(p -> p.getDislikeCount()).sum();
    object.put("dislikesCount", dislikesCount);

    int viewsCount = postList.stream().mapToInt(p -> p.getViewCount()).sum();
    object.put("viewsCount", viewsCount);

    LocalDateTime firstPublication = postRepository.findFirstMyPublication(currentUser.getId());

    if (firstPublication != null) {
      ZonedDateTime timeZoned = firstPublication.atZone(ZoneId.systemDefault());
      ZonedDateTime utcZoned = timeZoned.withZoneSameInstant(ZoneId.of("UTC"));

      object.put("firstPublication", utcZoned.toInstant().getEpochSecond());
    } else {
      object.put("firstPublication", 0);
    }
    return object;
  }

  public JsonNode getAllStatistics() {
    ObjectMapper mapper = new ObjectMapper();
    ObjectNode object = mapper.createObjectNode();
    List<Post> posts = postRepository.findAll();
    List<ResponsePostApi> allPosts = posts.stream()
        .map(p -> postMapper.postToResponsePostApi(p)).collect(Collectors.toList());
    ;
    object.put("postsCount", allPosts.size());
    List<ResponsePostApi> postList = commentMapper
        .addCommentsCountAndLikesForPosts(allPosts);
    int likesCount = postList.stream().mapToInt(p -> p.getLikeCount()).sum();
    object.put("likesCount", likesCount);

    int dislikesCount = postList.stream().mapToInt(p -> p.getDislikeCount()).sum();
    object.put("dislikesCount", dislikesCount);

    int viewsCount = postList.stream().mapToInt(p -> p.getViewCount()).sum();
    object.put("viewsCount", viewsCount);

    LocalDateTime firstPublication = postRepository.findFirstPublication();

    if (firstPublication != null) {
      ZonedDateTime timeZoned = firstPublication.atZone(ZoneId.systemDefault());
      ZonedDateTime utcZoned = timeZoned.withZoneSameInstant(ZoneId.of("UTC"));

      object.put("firstPublication", utcZoned.toInstant().getEpochSecond());
    } else {
      object.put("firstPublication", 0);
    }
    return object;
  }

  public JsonNode logout() {
    ObjectMapper mapper = new ObjectMapper();
    ObjectNode object = mapper.createObjectNode();
    String sessionId = request.getSession().getId();
    authorizedUsers.remove(sessionId);
    object.put("result", true);
    return object;
  }

  public JsonNode getSettings() throws Exception {
    GlobalSettings globalSettingStatistics = globalSettingsRepository.findAll().stream()
        .filter(p -> p.getCode().equals("STATISTICS_IS_PUBLIC")).findAny().get();
    if (globalSettingStatistics.getValue().equals("1")) {
      ObjectMapper mapper = new ObjectMapper();
      ObjectNode object = mapper.createObjectNode();
      List<GlobalSettings> globalSettings = globalSettingsRepository.findAll();
      for (GlobalSettings globalSetting : globalSettings) {
        object.put(globalSetting.getCode(), stringToBoolean(globalSetting.getValue()));
      }
      return object;
    } else {
      throw new Exception("Statistics is not public");
    }
  }


  @Transactional
  public void putSettings(boolean multiuserMode, boolean postPremoderation,
      boolean statisticsIsPublic)
      throws Exception {
    User currentUser = authenticationService.getCurrentUser();
    if (currentUser.getIsModerator() == 1) {
      List<GlobalSettings> globalSettings = globalSettingsRepository.findAll();
      for (GlobalSettings globalSetting : globalSettings) {
        if (globalSetting.getCode().equals("MULTIUSER_MODE")
            && String.valueOf(multiuserMode) != null) {
          globalSetting.setValue(booleanToString(multiuserMode));
        }
        if (globalSetting.getCode().equals("POST_PREMODERATION") &&
            String.valueOf(postPremoderation) != null) {
          globalSetting.setValue(booleanToString(postPremoderation));
        }
        if (globalSetting.getCode().equals("STATISTICS_IS_PUBLIC") &&
            String.valueOf(statisticsIsPublic) != null) {
          globalSetting.setValue(booleanToString(statisticsIsPublic));
        }
      }
    }
  }

  public JsonNode getCaptcha() throws IOException {
    ObjectMapper mapper = new ObjectMapper();
    ObjectNode object = mapper.createObjectNode();
    String code = createCaptchaValue(4);
    String secretCode = createCaptchaValue(22);
    CaptchaCode captchaCode = CaptchaCode.builder()
        .time(LocalDateTime.now().plusHours(3))
        .code(code)
        .secretCode(secretCode)
        .build();
    captchaCodeRepository.save(captchaCode);

    byte[] imageCaptcha = CaptchaCreateImage.getCaptcha(code, "png");
    String encodedString = Base64.getEncoder().encodeToString(imageCaptcha);
    object.put("secret", secretCode);
    object.put("image", "data:image/png;base64, " + encodedString);

    captchaCodeRepository.deleteByTimeBefore(LocalDateTime.now().plusHours(2));
    return object;
  }

  private boolean stringToBoolean(String string) {
    if (string.equals("1")) {
      return true;
    } else {
      return false;
    }
  }

  private String booleanToString(boolean value) throws Exception {
    if (String.valueOf(value).equals("true")) {
      return "1";
    }
    if (String.valueOf(value).equals("false")) {
      return "0";
    } else {
      throw new Exception("impossible value");
    }
  }

  public static String createCaptchaValue(int size) {
    Random random = new Random();
    int lenght = size + (Math.abs(random.nextInt()) % 2);
    StringBuffer captchaStrBuffer = new StringBuffer();
    for (int i = 0; i < lenght; i++) {
      int baseCharacterNumber = Math.abs(random.nextInt()) % 62;
      int characterNumber = 0;
      if (baseCharacterNumber < 26) {
        characterNumber = 65 + baseCharacterNumber;
      } else if (baseCharacterNumber < 52) {
        characterNumber = 97 + (baseCharacterNumber - 26);
      } else {
        characterNumber = 48 + (baseCharacterNumber - 52);
      }
      captchaStrBuffer.append((char) characterNumber);
    }
    return captchaStrBuffer.toString();
  }

}
