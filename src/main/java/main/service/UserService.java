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
import main.dto.request.PostProfileRequest;
import main.dto.request.PostProfileRequestWithPhoto;
import main.dto.request.RegisterFormRequest;
import main.dto.response.Errors;
import main.dto.response.ResponsePostApi;
import main.dto.response.ResultResponse;
import main.dto.response.ResultResponseWithErrors;
import main.dto.response.ResultResponseWithUserDto;
import main.dto.response.UserDto;
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
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Service
@Slf4j
public class UserService implements UserDetailsService {

  @Value("${user.password.minLength}")
  private int minPassword;

  @Value("${user.name.maxLength}")
  private int maxName;

  @Value("${user.name.minLength}")
  private int minName;

  private static final String SHORT_PASSWORD = "Пароль короче 6-ти символов";

  private static final String WRONG_CAPTCHA = "Код с картинки введён неверно";

  final
  UserRepository userRepository;

  final
  SecurityConfiguration securityConfiguration;

  final
  GlobalSettingsRepository globalSettingsRepository;

  final
  PostMapper postMapper;

  final
  CaptchaCodeRepository captchaCodeRepository;

  final
  HttpServletRequest request;

  final
  MailSender mailSender;

  final
  PostRepository postRepository;

  final
  AuthenticationService authenticationService;

  public UserService(UserRepository userRepository, SecurityConfiguration securityConfiguration,
      GlobalSettingsRepository globalSettingsRepository, PostMapper postMapper,
      CaptchaCodeRepository captchaCodeRepository, HttpServletRequest request,
      MailSender mailSender, PostRepository postRepository,
      AuthenticationService authenticationService) {
    this.userRepository = userRepository;
    this.securityConfiguration = securityConfiguration;
    this.globalSettingsRepository = globalSettingsRepository;
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

  public ResultResponseWithErrors saveUser(RegisterFormRequest registerFormUser) {
    GlobalSettings multiUserMode = globalSettingsRepository.findByCode("MULTIUSER_MODE")
        .orElseThrow(EntityNotFoundException::new);
    ResultResponseWithErrors resultResponseWithErrors = new ResultResponseWithErrors();
    if (multiUserMode.getValue().equals("YES")) {
      Errors errors = new Errors();
      Optional<User> byEmail = userRepository.findByEmail(registerFormUser.getEmail());
      if (byEmail.isEmpty() && registerFormUser.getPassword().length() >= minPassword
          && registerFormUser.getName().length() >= minName
          && registerFormUser.getName().length() < maxName) {
        User user = new User();
        user.setEmail(registerFormUser.getEmail());
        user.setName(registerFormUser.getName());
        user.setPhoto("/api/image/default-1.png");
        user.setRole(new Role(1, "ROLE_USER"));
        user.setRegTime(LocalDateTime.now().plusHours(3));
        user.setPassword(
            securityConfiguration.bcryptPasswordEncoder().encode(registerFormUser.getPassword()));
        user.setCode(UUID.randomUUID().toString());
        userRepository.save(user);
        resultResponseWithErrors.resultSuccess();
      } else {
        if (byEmail.isPresent()) {
          errors.setEmail("Этот e-mail уже зарегистрирован");
        }
        if (registerFormUser.getPassword().length() < minPassword) {
          errors.setPassword(SHORT_PASSWORD);
        }
        if (registerFormUser.getName().length() < minName
            || registerFormUser.getName().length() > maxName) {
          errors.setName("Имя указано неверно");
        }
        resultResponseWithErrors.setErrors(errors);
      }
    }
    return resultResponseWithErrors;
  }

  public ResultResponseWithUserDto login(LoginRequest loginDto) {
    ResultResponseWithUserDto resultResponseWithUserDto = new ResultResponseWithUserDto();
    User currentUser = userRepository.findByEmail(loginDto.getEmail())
        .orElseThrow(EntityNotFoundException::new);
    if (securityConfiguration.bcryptPasswordEncoder()
        .matches(loginDto.getPassword(), currentUser.getPassword())) {
      String sessionId = request.getSession().getId();
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
      resultResponseWithUserDto.resultSuccess();
      resultResponseWithUserDto.setUser(userDto);
    }
    return resultResponseWithUserDto;
  }

  public ResultResponseWithUserDto check() {
    ResultResponseWithUserDto resultResponseWithUserDto = new ResultResponseWithUserDto();
    String sessionId = request.getSession().getId();
    if (authenticationService.getAuthorizedUsers().containsKey(sessionId)) {
      Integer id = authenticationService.getAuthorizedUsers().get(sessionId);
      User user = userRepository.findById(id).orElseThrow(EntityNotFoundException::new);
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
      resultResponseWithUserDto.resultSuccess();
      resultResponseWithUserDto.setUser(userDto);
    }
    return resultResponseWithUserDto;
  }

  public ResultResponse restore(String email) {
    ResultResponse resultResponse = new ResultResponse();
    User userByEmail = userRepository.findByEmail(email).orElseThrow(EntityNotFoundException::new);
    mailSender.send(email, "Code", "https://philipp-skillbox.herokuapp.com/login/change-password/"
        + userByEmail.getCode());
    resultResponse.resultSuccess();
    return resultResponse;
  }

  @Transactional
  public ResultResponseWithErrors postNewPassword(ChangePasswordRequest changePasswordDto) {
    ResultResponseWithErrors resultResponseWithErrors = new ResultResponseWithErrors();
    Errors errors = new Errors();

    Optional<User> userByCode = userRepository.findByCode(changePasswordDto.getCode());
    Optional<CaptchaCode> captchaCode = captchaCodeRepository
        .findByCode(changePasswordDto.getCaptcha());
    if (userByCode.isPresent() && captchaCode.isPresent()
        && changePasswordDto.getPassword().length() >= minPassword
        && captchaCode.get().getSecretCode().equals(changePasswordDto.getCaptcha_secret())) {
      User user = userRepository.getOne(userByCode.get().getId());
      user.setPassword(
          securityConfiguration.bcryptPasswordEncoder().encode(changePasswordDto.getPassword()));

      resultResponseWithErrors.resultSuccess();
    } else {
      if (changePasswordDto.getPassword().length() < minPassword) {
        errors.setPassword(SHORT_PASSWORD);
      }
      if (captchaCode.isPresent() && !captchaCode.get().getSecretCode()
          .equals(changePasswordDto.getCaptcha_secret())
          || captchaCode.isEmpty()) {
        errors.setCaptcha(WRONG_CAPTCHA);
      }
      if (userByCode.isEmpty()) {
        errors.setCode("Ссылка для восстановления пароля устарела."
            + "     <a href=     \"/auth/restore\">Запросить ссылку снова</a>");
      }
      resultResponseWithErrors.setErrors(errors);
    }
    return resultResponseWithErrors;
  }

  @Transactional
  public ResultResponseWithErrors postNewProfileWithPhoto(
      PostProfileRequestWithPhoto postProfileRequest)
      throws Exception {
    ResultResponseWithErrors resultResponseWithErrors = new ResultResponseWithErrors();
    Errors errors = new Errors();

    String sessionId = request.getSession().getId();
    Integer id = authenticationService.getAuthorizedUsers().get(sessionId);
    User user = userRepository.findById(id).orElseThrow(EntityNotFoundException::new);
    Optional<User> userByEmail = userRepository.findByEmail(postProfileRequest.getEmail());
    if (userByEmail.isPresent() && postProfileRequest.getName().length() > 0 &&
        postProfileRequest.getName().length() < maxName &&
        postProfileRequest.getPassword() != null && postProfileRequest.getPhoto() != null
        && postProfileRequest.getRemovePhoto() == 0) {
      User userToUpdate = userRepository.getOne(user.getId());
      userToUpdate.setPassword(
          securityConfiguration.bcryptPasswordEncoder().encode(postProfileRequest.getPassword()));
      String fileName = uploadImageWithResize(postProfileRequest.getPhoto(), userToUpdate.getId());
      File file = new File(fileName);
      userToUpdate.setPhoto("/api/image/" + file.getName());

      resultResponseWithErrors.resultSuccess();
    } else {
      if (userByEmail.isPresent() && !user.getEmail().equals(userByEmail.get().getEmail())) {
        errors.setEmail("Этот e-mail уже зарегистрирован");
      }
      if (postProfileRequest.getName().length() < minName
          || postProfileRequest.getName().length() > maxName) {
        errors.setName("Имя указано неверно");
      }
      if (postProfileRequest.getName().length() < minPassword) {
        errors.setPassword("Пароль короче 6-ти символов");
      }
      resultResponseWithErrors.setErrors(errors);
    }
    return resultResponseWithErrors;
  }

  @Transactional
  public ResultResponseWithErrors postNewProfile(PostProfileRequest profileRequest) {
    ResultResponseWithErrors resultResponseWithErrors = new ResultResponseWithErrors();
    Errors errors = new Errors();

    String sessionId = request.getSession().getId();
    Integer id = authenticationService.getAuthorizedUsers().get(sessionId);
    User user = userRepository.findById(id).orElseThrow(EntityNotFoundException::new);
    Optional<User> userByEmail = userRepository.findByEmail(profileRequest.getEmail());
    if (profileRequest.getPassword() == null && profileRequest.getPhoto() == null &&
        profileRequest.getRemovePhoto() == null && profileRequest.getName() != null &&
        profileRequest.getName().length() > 0 && profileRequest.getName().length() < maxName) {
      User userToUpdate = userRepository.getOne(user.getId());
      userToUpdate.setName(profileRequest.getName());
      resultResponseWithErrors.resultSuccess();
    } else if (profileRequest.getPassword() != null && profileRequest.getPhoto() == null &&
        profileRequest.getRemovePhoto() == null && profileRequest.getName() != null &&
        profileRequest.getName().length() >= minName && profileRequest.getName().length() < maxName
        &&
        profileRequest.getPassword().length() >= minPassword) {
      User userToUpdate = userRepository.getOne(user.getId());
      userToUpdate.setPassword(
          securityConfiguration.bcryptPasswordEncoder().encode(profileRequest.getPassword()));
      resultResponseWithErrors.resultSuccess();
    } else if (profileRequest.getPassword() == null && profileRequest.getPhoto().equals("") &&
        profileRequest.getRemovePhoto() == 1 && profileRequest.getName() != null &&
        profileRequest.getName().length() > 0 && profileRequest.getName().length() < maxName) {
      User userToUpdate = userRepository.getOne(user.getId());
      userToUpdate.setPhoto(null);
      resultResponseWithErrors.resultSuccess();
    } else {
      if (userByEmail.isPresent() && !user.getEmail().equals(userByEmail.get().getEmail())) {
        errors.setEmail("Этот e-mail уже зарегистрирован");
      }
      if (profileRequest.getName().length() < 1
          || profileRequest.getName().length() > maxName) {
        errors.setName("Имя указано неверно");
      }
      if (profileRequest.getPassword().length() < minPassword) {
        errors.setPassword("Пароль короче 6-ти символов");
      }
      resultResponseWithErrors.setErrors(errors);
    }
    return resultResponseWithErrors;
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

  public JsonNode getMyStatistics() throws EntityNotFoundException {
    String sessionId = request.getSession().getId();
    Integer id = authenticationService.getAuthorizedUsers().get(sessionId);
    User currentUser = userRepository.findById(id).orElseThrow(EntityNotFoundException::new);
    ObjectNode object = createObjectNode();
    List<Post> myPosts = postRepository.findAllMyPosts(0, (int) postRepository.count(),
        "ACCEPTED", currentUser.getId());
    List<ResponsePostApi> postList = myPosts.stream()
        .map(p -> postMapper.postToResponsePostApi(p))
        .collect(Collectors.toList());
    object.put("postsCount", myPosts.size());

    int likesCount = postList.stream().mapToInt(ResponsePostApi::getLikeCount).sum();
    object.put("likesCount", likesCount);

    int dislikesCount = postList.stream().mapToInt(ResponsePostApi::getDislikeCount).sum();
    object.put("dislikesCount", dislikesCount);

    int viewsCount = postList.stream().mapToInt(ResponsePostApi::getViewCount).sum();
    object.put("viewsCount", viewsCount);

    LocalDateTime firstPublication = postRepository
        .findFirstMyPublication(currentUser.getId());

    if (firstPublication != null) {
      ZonedDateTime timeZoned = firstPublication.atZone(ZoneId.systemDefault()).minusHours(3);
      ZonedDateTime utcZoned = timeZoned.withZoneSameInstant(ZoneId.of("UTC"));

      object.put("firstPublication", utcZoned.toInstant().getEpochSecond());
    } else {
      object.put("firstPublication", 0);
    }
    return object;
  }

  public JsonNode getAllStatistics() throws EntityNotFoundException {
    GlobalSettings globalSettings = globalSettingsRepository.findByCode("STATISTICS_IS_PUBLIC")
        .orElseThrow(EntityNotFoundException::new);
    if (globalSettings.getValue().equals("YES")) {
      return getStatAll();
    }
    if (globalSettings.getValue().equals("NO")) {
      String sessionId = request.getSession().getId();
      Integer id = authenticationService.getAuthorizedUsers().get(sessionId);
      User currentUser = userRepository.findById(id).orElseThrow(EntityNotFoundException::new);
      if (currentUser.getIsModerator() == 1) {
        return getStatAll();
      } else {
        throw new EntityNotFoundException("User is not authorized or user is not moderator");
      }
    } else {
      throw new EntityNotFoundException("Statistics is not public or user is not authorized");
    }
  }

  public ResultResponse logout() {
    ResultResponse resultResponse = new ResultResponse();
    String sessionId = request.getSession().getId();
    Map<String, Integer> authorizedUsers = authenticationService.getAuthorizedUsers();
    authorizedUsers.remove(sessionId);
    authenticationService.setAuthorizedUsers(authorizedUsers);
    resultResponse.resultSuccess();
    return resultResponse;
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
    User currentUser = userRepository.findById(id).orElseThrow(EntityNotFoundException::new);
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
        .time(correctTime(LocalDateTime.now()))
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
    int likesCount = allPosts.stream().mapToInt(ResponsePostApi::getLikeCount).sum();
    object.put("likesCount", likesCount);

    int dislikesCount = allPosts.stream().mapToInt(ResponsePostApi::getDislikeCount).sum();
    object.put("dislikesCount", dislikesCount);

    int viewsCount = allPosts.stream().mapToInt(ResponsePostApi::getViewCount).sum();
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

  private LocalDateTime correctTime(LocalDateTime time) {
    return time.plusHours(3L);
  }

}
