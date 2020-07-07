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
import java.util.ArrayList;
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
import main.api.response.PostListApi;
import main.api.response.ResponsePostApi;
import main.dto.NewProfileForm;
import main.dto.RegisterForm;
import main.dto.UserDto;
import main.mapper.CommentMapper;
import main.mapper.PostMapper;
import main.model.CaptchaCode;
import main.model.GlobalSettings;
import main.model.Role;
import main.model.User;
import main.repository.CaptchaCodeRepository;
import main.repository.GlobalSettingsRepository;
import main.repository.PostRepository;
import main.repository.UserRepository;
import org.imgscalr.Scalr;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.acls.model.NotFoundException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
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

  private static Map<String, Integer> authorizedUsers = new HashMap<>();

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
  private InitService initService;

  @Autowired
  PostService postService;

  @Autowired
  PostRepository postRepository;


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

    Optional<User> byEmail = userRepository.findByEmail(registerFormUser.getEmail());
    if (byEmail.isEmpty() && registerFormUser.getPassword().length() > 5
        && registerFormUser.getName().length() > 0 && registerFormUser.getName().length() < 1000) {
      User user = new User();
      user.setEmail(registerFormUser.getEmail());
      user.setName(registerFormUser.getName());
      user.setRole(new Role(1, "ROLE_USER"));
      user.setRegTime(LocalDateTime.now());
      user.setPassword(passwordEncoder().encode(registerFormUser.getPassword()));
      user.setCode(UUID.randomUUID().toString());
      userRepository.save(user);
      object.put("result", true);
    } else {
      object.put("result", false);
      if (!byEmail.isEmpty()) {
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

  public User getCurrentUser() throws Exception {
    Authentication auth = SecurityContextHolder.getContext().getAuthentication();

    if (null == auth) {
      throw new NotFoundException("");
    }

    Object obj = auth.getPrincipal();
    String username = "";

    if (obj instanceof UserDetails) {
      username = ((UserDetails) obj).getUsername();
    } else {
      username = obj.toString();
    }

    User us = userRepository.findByEmail(username).get();
    return us;
  }

  public JsonNode login(String email, String password) {
    ObjectMapper mapper = new ObjectMapper();
    ObjectNode object = mapper.createObjectNode();
    Optional<User> userByEmail = userRepository.findByEmail(email);
    if (!userByEmail.isEmpty() && passwordEncoder()
        .matches(password, userByEmail.get().getPassword())) {
      String sessionId = request.getSession().getId();
      User currentUser = userByEmail.get();
      authorizedUsers.put(sessionId, currentUser.getId());
      UserDto userDto = UserDto.builder()
          .id(currentUser.getId())
          .name(currentUser.getName())
          .photo(currentUser.getPhoto())
          .email(currentUser.getEmail())
          .moderation(true)
          .moderationCount(21)
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

      UserDto userDto = UserDto.builder()
          .id(id)
          .name(user.getName())
          .photo(user.getPhoto())
          .email(user.getEmail())
          .moderation(true)
          .moderationCount(21)
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

  public JsonNode restore(String email) {
    ObjectMapper mapper = new ObjectMapper();
    ObjectNode object = mapper.createObjectNode();
    Optional<User> userByEmail = userRepository.findByEmail(email);
    if (!userByEmail.isEmpty()) {
      mailSender.send(email, "Code", "/login/change-password/" + userByEmail.get().getCode());
      object.put("result", true);
      return object;
    } else {
      object.put("result", false);
      return object;
    }
  }

  @Transactional
  public JsonNode postNewPassword(String code, String password, Integer captcha,
      Integer captcha_secret) {
    ObjectMapper mapper = new ObjectMapper();
    ObjectNode object = mapper.createObjectNode();
    ObjectNode objectError = mapper.createObjectNode();
    Optional<User> userByCode = userRepository.findByCode(code);
    Optional<CaptchaCode> captchaCode = captchaCodeRepository.findByCode(captcha);
    if (!userByCode.isEmpty() && !captchaCode.isEmpty() && password.length() > 5
        && captchaCode.get().getSecretCode() == captcha_secret) {
      User user = userRepository.getOne(userByCode.get().getId());
      user.setPassword(passwordEncoder().encode(password));

      object.put("result", true);
    } else {
      object.put("result", false);
      if (password.length() < 6) {
        objectError.put("password", SHORT_PASSWORD);
      }
      if (!captchaCode.isEmpty() && captchaCode.get().getSecretCode() != captcha_secret
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
  public JsonNode postNewProfile(NewProfileForm newProfileForm, MultipartFile photo)
      throws Exception {
    ObjectMapper mapper = new ObjectMapper();
    ObjectNode object = mapper.createObjectNode();
    ObjectNode objectError = mapper.createObjectNode();
    User user = getCurrentUser();
    Optional<User> userByEmail = userRepository.findByEmail(newProfileForm.getEmail());
    if (userByEmail.isEmpty() && newProfileForm.getName() != null
        && newProfileForm.getName().length() > 0 &&
        newProfileForm.getName().length() < 1000 && newProfileForm.getEmail() != null &&
        newProfileForm.getPassword() == null && newProfileForm.getPhoto() == null &&
        newProfileForm.getRemovePhoto() == null) {
      User userToUpdate = userRepository.getOne(user.getId());
      userToUpdate.setName(newProfileForm.getName());
      userToUpdate.setEmail(newProfileForm.getEmail());
      object.put("result", true);
    }
    if (newProfileForm.getName() != null && newProfileForm.getName().length() > 0 &&
        newProfileForm.getName().length() < 1000 && newProfileForm.getEmail() != null &&
        newProfileForm.getPassword() != null && newProfileForm.getPhoto() == null &&
        newProfileForm.getRemovePhoto() == null) {
      User userToUpdate = userRepository.getOne(user.getId());
      userToUpdate.setPassword(passwordEncoder().encode(newProfileForm.getPassword()));
      object.put("result", true);
    }
    if (newProfileForm.getName() != null && newProfileForm.getName().length() > 0 &&
        newProfileForm.getName().length() < 1000 && newProfileForm.getEmail() != null &&
        newProfileForm.getPassword() == null && newProfileForm.getPhoto() != null &&
        newProfileForm.getRemovePhoto() == 1) {
      User userToUpdate = userRepository.getOne(user.getId());
      userToUpdate.setPhoto(null);
      object.put("result", true);
    }
    if (newProfileForm.getName() != null && newProfileForm.getName().length() > 0 &&
        newProfileForm.getName().length() < 1000 && newProfileForm.getEmail() != null &&
        newProfileForm.getPassword() != null && newProfileForm.getPhoto() == null &&
        newProfileForm.getRemovePhoto() != null && !photo.isEmpty()) {
      User userToUpdate = userRepository.getOne(user.getId());
      userToUpdate.setPhoto(uploadImage(photo));
      userToUpdate.setPassword(passwordEncoder().encode(newProfileForm.getPassword()));
      object.put("result", true);
    }
    if (!userByEmail.isEmpty() && !user.getEmail().equals(userByEmail.get().getEmail())
        || newProfileForm.getName().length() < 1
        || newProfileForm.getName().length() > 1000 || newProfileForm.getPassword().length() < 6) {
      object.put("result", false);
      if (!userByEmail.isEmpty() && !user.getEmail().equals(userByEmail.get().getEmail())) {
        objectError.put("email", "Этот e-mail уже зарегистрирован");
      }
      if (newProfileForm.getName().length() < 1
          || newProfileForm.getName().length() > 1000) {
        objectError.put("name", "Имя указано неверно");
      }
      if (newProfileForm.getPassword().length() < 6) {
        objectError.put("password", "Пароль короче 6-ти символов");
      }
      object.put("errors", objectError);
    }
    return object;
  }

  private String uploadImage(MultipartFile image) throws IOException {
    byte[] byteArr = image.getBytes();
    InputStream inputStream = new ByteArrayInputStream(byteArr);
    BufferedImage bufferedImage = ImageIO.read(inputStream);
    BufferedImage scaledImage = Scalr.resize(bufferedImage, 100);
    List<String> listDirs = List.of(randomLetter(), randomLetter(), randomLetter());
    File dir = new File("src/main/resources/static/upload");
    if (!dir.exists()) {
      dir.mkdir();
    }
    for (String listDir : listDirs) {
      File theDir = new File(dir + "/" + listDir);
      if (!theDir.exists()) {
        theDir.mkdir();
      }
      dir = theDir;
    }
    Random random = new Random();
    File filePath = new File(dir + "/" + random.nextInt(100000) + ".jpg");
    ImageIO.write(scaledImage, "jpg", filePath);
    return filePath.toString();
  }

  private String randomLetter() {
    Random random = new Random();
    char c = (char) (random.nextInt(26) + 'a');
    return String.valueOf(c);
  }

  public JsonNode getMyStatistics() throws Exception {
    ObjectMapper mapper = new ObjectMapper();
    ObjectNode object = mapper.createObjectNode();
    PostListApi postListApi = postService.getAllMyPosts(PageRequest.of
        (0, (int) postRepository.count()), "published");
    List<ResponsePostApi> postList = postListApi.getPostList();
    object.put("postsCount", postList.size());

    int likesCount = postList.stream().mapToInt(p -> p.getLikeCount()).sum();
    object.put("likesCount", likesCount);

    int dislikesCount = postList.stream().mapToInt(p -> p.getDislikeCount()).sum();
    object.put("dislikesCount", dislikesCount);

    int viewsCount = postList.stream().mapToInt(p -> p.getViewCount()).sum();
    object.put("viewsCount", viewsCount);

    String firstPublication = postList.stream().map(p -> p.getTime()).min(LocalDateTime::compareTo)
        .get().toString();
    object.put("firstPublication", firstPublication);
    return object;
  }

  public JsonNode getAllStatistics() {
    ObjectMapper mapper = new ObjectMapper();
    ObjectNode object = mapper.createObjectNode();
    List<ResponsePostApi> allPosts = postRepository.findAll().stream()
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

    String firstPublication = postList.stream().map(p -> p.getTime()).min(LocalDateTime::compareTo)
        .get().toString();
    object.put("firstPublication", firstPublication);
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
    User currentUser = getCurrentUser();
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
}
