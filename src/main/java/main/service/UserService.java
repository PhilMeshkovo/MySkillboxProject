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
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Random;
import java.util.UUID;
import java.util.stream.Collectors;
import javax.imageio.ImageIO;
import javax.persistence.EntityNotFoundException;
import javax.servlet.http.HttpServletRequest;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import main.config.SecurityConfiguration;
import main.dto.request.ChangePasswordRequest;
import main.dto.request.GlobalSettingsRequest;
import main.dto.request.LoginRequest;
import main.dto.request.RegisterFormRequest;
import main.dto.response.ResponsePostApi;
import main.dto.response.UserDto;
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
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Service
@Slf4j
public class UserService implements UserDetailsService {

  private static final String SHORT_PASSWORD = "Пароль короче 6-ти символов";

  private static final String WRONG_CAPTCHA = "Код с картинки введён неверно";

  private final UserRepository userRepository;

  private final SecurityConfiguration securityConfiguration;

  private final GlobalSettingsRepository globalSettingsRepository;

  private final CommentMapper commentMapper;

  private final PostMapper postMapper;

  private final CaptchaCodeRepository captchaCodeRepository;

  private final HttpServletRequest request;

  private final MailSender mailSender;

  private final PostRepository postRepository;

  private final AuthenticationService authenticationService;

  @Autowired
  public UserService(UserRepository userRepository,
      SecurityConfiguration securityConfiguration,
      GlobalSettingsRepository globalSettingsRepository, CommentMapper commentMapper,
      PostMapper postMapper, CaptchaCodeRepository captchaCodeRepository,
      HttpServletRequest request, MailSender mailSender,
      PostRepository postRepository, AuthenticationService authenticationService) {
    this.userRepository = userRepository;
    this.securityConfiguration = securityConfiguration;
    this.globalSettingsRepository = globalSettingsRepository;
    this.commentMapper = commentMapper;
    this.postMapper = postMapper;
    this.captchaCodeRepository = captchaCodeRepository;
    this.request = request;
    this.mailSender = mailSender;
    this.postRepository = postRepository;
    this.authenticationService = authenticationService;
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

  public JsonNode saveUser(RegisterFormRequest registerFormUser) {
    Optional<GlobalSettings> globalSettings = globalSettingsRepository.findById(1);
    GlobalSettings multiUserMode = globalSettings.orElseThrow();
    if (multiUserMode.getValue().equals("YES")) {
      ObjectMapper mapper = new ObjectMapper();
      ObjectNode object = mapper.createObjectNode();
      ObjectNode objectError = mapper.createObjectNode();
      Optional<User> byEmail = userRepository.findByEmail(registerFormUser.getE_mail());
      if (byEmail.isEmpty() && registerFormUser.getPassword().length() > 5
          && registerFormUser.getName().length() > 0
          && registerFormUser.getName().length() < 1000) {
        User user = new User();
        user.setEmail(registerFormUser.getE_mail());
        user.setName(registerFormUser.getName());
        user.setRole(new Role(1, "ROLE_USER"));
        user.setRegTime(LocalDateTime.now().plusHours(3));
        user.setPassword(
            securityConfiguration.bcryptPasswordEncoder().encode(registerFormUser.getPassword()));
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
    } else {
      throw new EntityNotFoundException("MULTIUSER MODE OFF");
    }
  }

  public JsonNode login(LoginRequest loginDto) {
    ObjectMapper mapper = new ObjectMapper();
    ObjectNode object = mapper.createObjectNode();
    Optional<User> userByEmail = userRepository.findByEmail(loginDto.getE_mail());
    if (userByEmail.isPresent() && securityConfiguration.bcryptPasswordEncoder()
        .matches(loginDto.getPassword(), userByEmail.get().getPassword())) {
      String sessionId = request.getSession().getId();
      User currentUser = userByEmail.get();
      Map<String, Integer> authorizedUsers = authenticationService.getAuthorizedUsers();
      authorizedUsers.put(sessionId, currentUser.getId());
      authenticationService.setAuthorizedUsers(authorizedUsers);
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
    if (authenticationService.getAuthorizedUsers().containsKey(sessionId)) {
      Integer id = authenticationService.getAuthorizedUsers().get(sessionId);
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
    ObjectNode object = createObjectNode();
    Optional<User> userByEmail = userRepository.findByEmail(email);
    if (userByEmail.isPresent()) {
      mailSender.send(email, "Code", "https://philipp-skillbox.herokuapp.com/login/change-password/"
          + userByEmail.get().getCode());
      object.put("result", true);
    } else {
      object.put("result", false);
    }
    return object;
  }

  @Transactional
  public JsonNode postNewPassword(ChangePasswordRequest changePasswordDto) {
    ObjectMapper mapper = new ObjectMapper();
    ObjectNode object = mapper.createObjectNode();
    ObjectNode objectError = mapper.createObjectNode();
    Optional<User> userByCode = userRepository.findByCode(changePasswordDto.getCode());
    Optional<CaptchaCode> captchaCode = captchaCodeRepository
        .findByCode(changePasswordDto.getCaptcha());
    if (userByCode.isPresent() && captchaCode.isPresent()
        && changePasswordDto.getPassword().length() > 5
        && captchaCode.get().getSecretCode().equals(changePasswordDto.getCaptcha_secret())) {
      User user = userRepository.getOne(userByCode.get().getId());
      user.setPassword(
          securityConfiguration.bcryptPasswordEncoder().encode(changePasswordDto.getPassword()));

      object.put("result", true);
    } else {
      object.put("result", false);
      if (changePasswordDto.getPassword().length() < 6) {
        objectError.put("password", SHORT_PASSWORD);
      }
      if (captchaCode.isPresent() && !captchaCode.get().getSecretCode()
          .equals(changePasswordDto.getCaptcha_secret())
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
    String sessionId = request.getSession().getId();
    Integer id = authenticationService.getAuthorizedUsers().get(sessionId);
    User user = userRepository.findById(id).get();
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
      userToUpdate.setPassword(securityConfiguration.bcryptPasswordEncoder().encode(password));
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
        removePhoto == 0) {
      User userToUpdate = userRepository.getOne(user.getId());
      String fileName = uploadImageWithResize(photo, userToUpdate.getId());
      File file = new File(fileName);
      userToUpdate.setPhoto("/api/image/" + file.getName());
      userToUpdate.setPassword(securityConfiguration.bcryptPasswordEncoder().encode(password));
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
    String sessionId = request.getSession().getId();
    Integer id = authenticationService.getAuthorizedUsers().get(sessionId);
    Optional<User> currentUser = userRepository.findById(id);
    if (currentUser.isPresent()) {
      ObjectNode object = createObjectNode();
      List<Post> myPosts = postRepository.findAllMyPosts(0, (int) postRepository.count(),
          "ACCEPTED", currentUser.get().getId());
      List<ResponsePostApi> postList = myPosts.stream()
          .map(p -> postMapper.postToResponsePostApi(p))
          .collect(Collectors.toList());
      object.put("postsCount", myPosts.size());

      int likesCount = postList.stream().mapToInt(p -> p.getLikeCount()).sum();
      object.put("likesCount", likesCount);

      int dislikesCount = postList.stream().mapToInt(p -> p.getDislikeCount()).sum();
      object.put("dislikesCount", dislikesCount);

      int viewsCount = postList.stream().mapToInt(p -> p.getViewCount()).sum();
      object.put("viewsCount", viewsCount);

      LocalDateTime firstPublication = postRepository
          .findFirstMyPublication(currentUser.get().getId());

      if (firstPublication != null) {
        ZonedDateTime timeZoned = firstPublication.atZone(ZoneId.systemDefault()).minusHours(3);
        ZonedDateTime utcZoned = timeZoned.withZoneSameInstant(ZoneId.of("UTC"));

        object.put("firstPublication", utcZoned.toInstant().getEpochSecond());
      } else {
        object.put("firstPublication", 0);
      }
      return object;
    } else {
      throw new Exception("User is not authorized");
    }
  }

  public JsonNode getAllStatistics() throws Exception {
    GlobalSettings globalSettings = globalSettingsRepository.findById(3).get();
    if (globalSettings.getValue().equals("YES")) {
      return getStatAll();
    }
    if (globalSettings.getValue().equals("NO")) {
      String sessionId = request.getSession().getId();
      Integer id = authenticationService.getAuthorizedUsers().get(sessionId);
      Optional<User> currentUser = userRepository.findById(id);
      if (currentUser.isPresent() && currentUser.get().getIsModerator() == 1) {
        return getStatAll();
      } else {
        throw new Exception("User is not authorized or user is not moderator");
      }
    } else {
      throw new Exception("Statistics is not public or user is not authorized");
    }
  }

  public JsonNode logout() {
    ObjectNode object = createObjectNode();
    String sessionId = request.getSession().getId();
    Map<String, Integer> authorizedUsers = authenticationService.getAuthorizedUsers();
    authorizedUsers.remove(sessionId);
    authenticationService.setAuthorizedUsers(authorizedUsers);
    object.put("result", true);
    return object;
  }

  public JsonNode getSettings() {
    ObjectNode object = createObjectNode();
    List<GlobalSettings> globalSettings = globalSettingsRepository.findAll();
    for (GlobalSettings globalSetting : globalSettings) {
      object.put(globalSetting.getCode(),
          stringToBoolean(globalSetting.getValue()));
    }
    return object;
  }


  @Transactional
  public void putSettings(GlobalSettingsRequest globalSettingsDto)
      throws Exception {
    String sessionId = request.getSession().getId();
    Integer id = authenticationService.getAuthorizedUsers().get(sessionId);
    User currentUser = userRepository.findById(id).get();
    if (currentUser.getIsModerator() == 1) {
      GlobalSettings globalSettingsMultiUser = globalSettingsRepository.getOne(1);
      if (globalSettingsDto.isMULTIUSER_MODE()) {
        globalSettingsMultiUser.setValue("YES");
      } else {
        globalSettingsMultiUser.setValue("NO");
      }
      GlobalSettings globalSettingsPostPremoderation = globalSettingsRepository.getOne(2);
      if (globalSettingsDto.isPOST_PREMODERATION()) {
        globalSettingsPostPremoderation.setValue("YES");
      } else {
        globalSettingsPostPremoderation.setValue("NO");
      }
      GlobalSettings globalSettingsStat = globalSettingsRepository.getOne(3);
      if (globalSettingsDto.isSTATISTICS_IS_PUBLIC()) {
        globalSettingsStat.setValue("YES");
      } else {
        globalSettingsStat.setValue("NO");
      }
    } else {
      throw new Exception("unauthorized");
    }
  }

  public JsonNode getCaptcha() throws IOException {
    ObjectNode object = createObjectNode();
    String code = createCaptchaValue(3);
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
    if (string.equals("YES")) {
      return true;
    } else {
      return false;
    }
  }

  public static String createCaptchaValue(int size) {
    Random random = new Random();
    int length = size + (Math.abs(random.nextInt()) % 2);
    StringBuffer captchaStrBuffer = new StringBuffer();
    for (int i = 0; i < length; i++) {
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

  private JsonNode getStatAll() {
    ObjectNode object = createObjectNode();
    List<Post> posts = postRepository.findAll();
    List<ResponsePostApi> allPosts = posts.stream()
        .map(p -> postMapper.postToResponsePostApi(p)).collect(Collectors.toList());
    object.put("postsCount", allPosts.size());
    List<ResponsePostApi> postList = commentMapper
        .addCommentsCountAndLikesForPosts(allPosts);
    int likesCount = postList.stream().mapToInt(ResponsePostApi::getLikeCount).sum();
    object.put("likesCount", likesCount);

    int dislikesCount = postList.stream().mapToInt(ResponsePostApi::getDislikeCount).sum();
    object.put("dislikesCount", dislikesCount);

    int viewsCount = postList.stream().mapToInt(ResponsePostApi::getViewCount).sum();
    object.put("viewsCount", viewsCount);

    LocalDateTime firstPublication = postRepository.findFirstPublication();

    if (firstPublication != null) {
      ZonedDateTime timeZoned = firstPublication.atZone(ZoneId.systemDefault()).minusHours(3);
      ZonedDateTime utcZoned = timeZoned.withZoneSameInstant(ZoneId.of("UTC"));

      object.put("firstPublication", utcZoned.toInstant().getEpochSecond());
    } else {
      object.put("firstPublication", 0);
    }
    return object;
  }

  private ObjectNode createObjectNode() {
    ObjectMapper mapper = new ObjectMapper();
    ObjectNode object = mapper.createObjectNode();
    return object;
  }
}
