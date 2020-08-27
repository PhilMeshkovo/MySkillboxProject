package main.service;

import com.fasterxml.jackson.databind.JsonNode;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import main.dto.LoginDto;
import main.dto.RegisterForm;
import main.model.CaptchaCode;
import main.model.GlobalSettings;
import main.model.Post;
import main.model.Role;
import main.model.User;
import main.model.enums.ModerationStatus;
import main.repository.CaptchaCodeRepository;
import main.repository.GlobalSettingsRepository;
import main.repository.PostRepository;
import main.repository.UserRepository;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentMatchers;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest
class UserServiceTest {

  @Autowired
  UserService userService;

  @MockBean
  UserRepository userRepository;

  @MockBean
  AuthenticationService authenticationService;

  @MockBean
  PostRepository postRepository;

  @MockBean
  MailSender mailSender;

  @MockBean
  CaptchaCodeRepository captchaCodeRepository;

  @MockBean
  GlobalSettingsRepository globalSettingsRepository;

  @Test
  void loadUserByUsername() {
    Mockito.doReturn(Optional.of(new User(1, 1, LocalDateTime.now(), "vanya",
        "some@mail.ru", "123456", "123456", "123.jpr", new Role(1))))
        .when(userRepository).findByEmail("some@mail.ru");
    UserDetails user = userService.loadUserByUsername("some@mail.ru");
    Assertions.assertEquals("some@mail.ru", user.getUsername());
  }


  @Test
  void saveUser() {
    GlobalSettings globalSettings = new GlobalSettings();
    globalSettings.setValue("YES");
    Mockito.doReturn(Optional.of(globalSettings))
        .when(globalSettingsRepository).findById(1);
    RegisterForm registerForm = new RegisterForm();
    registerForm.setE_mail("some@mail.ru");
    registerForm.setCaptcha("123456");
    registerForm.setCaptcha_secret("123456");
    registerForm.setName("vanya");
    registerForm.setPassword("12345");
    Mockito.doReturn(Optional.of(getUser()))
        .when(userRepository).findByEmail("some@mail.ru");
    Mockito.doReturn(getUser()).when(userRepository).save(getUser());
    JsonNode jsonNode = userService.saveUser(registerForm);
    Assertions.assertFalse(jsonNode.get("result").asBoolean());
    Assertions.assertEquals("Пароль короче 6-ти символов",
        jsonNode.get("errors").get("password").asText());
  }

  @Test
  void login() {
    Mockito.doReturn(Optional.of(new User(1, 1, LocalDateTime.now(), "vanya",
        "some@mail.ru", "$2a$10$FLwXXL.MI88B.UCf5zgHbek0Qk3k.oSqhzAUyyMPJFkYWOddpuLqu",
        "123456", "123.jpr", new Role(1))))
        .when(userRepository).findByEmail("some@mail.ru");
    LoginDto loginDto = new LoginDto();
    loginDto.setE_mail("some@mail.ru");
    loginDto.setPassword("123456");
    JsonNode jsonNode = userService.login(loginDto);
    Assertions.assertTrue(jsonNode.get("result").asBoolean());
    Assertions.assertTrue(jsonNode.get("user").get("settings").asBoolean());
  }

  @Test
  void check() {
    JsonNode jsonNode = userService.check();
    Assertions.assertFalse(jsonNode.get("result").asBoolean());
  }

  @Test
  void restore() {
    Mockito.doReturn(Optional.of(new User(1, 1, LocalDateTime.now(), "vanya",
        "some@mail.ru", "$2a$10$FLwXXL.MI88B.UCf5zgHbek0Qk3k.oSqhzAUyyMPJFkYWOddpuLqu",
        "123456", "123.jpr", new Role(1))))
        .when(userRepository).findByEmail("some@mail.ru");
    JsonNode jsonNode = userService.restore("some@mail.ru");
    Mockito.verify(mailSender, Mockito.times(1))
        .send("some@mail.ru", "Code", "/login/change-password/123456");
    Assertions.assertTrue(jsonNode.get("result").asBoolean());
  }

  @Test
  void postNewPassword() {
    Mockito.doReturn(Optional.of(new User(1, 1, LocalDateTime.now(), "vanya",
        "some@mail.ru", "$2a$10$FLwXXL.MI88B.UCf5zgHbek0Qk3k.oSqhzAUyyMPJFkYWOddpuLqu",
        "123456", "123.jpr", new Role(1))))
        .when(userRepository).findByCode("123456");
    Mockito.doReturn(Optional.of(new CaptchaCode(1, LocalDateTime.now(), "123456", "123456")))
        .when(captchaCodeRepository).findByCode("123456");
    Mockito.doReturn(new User(1, 1, LocalDateTime.now(), "vanya",
        "some@mail.ru", "$2a$10$FLwXXL.MI88B.UCf5zgHbek0Qk3k.oSqhzAUyyMPJFkYWOddpuLqu",
        "123456", "123.jpr", new Role(1)))
        .when(userRepository).getOne(1);
    JsonNode jsonNode = userService.postNewPassword("123456", "123456", "123456", "123456");
    Assertions.assertTrue(jsonNode.get("result").asBoolean());
  }

  @Test
  void getAllStatistics() throws Exception {
    Post post = newPost();
    GlobalSettings globalSettings = new GlobalSettings();
    globalSettings.setValue("YES");
    Mockito.doReturn(Optional.of(globalSettings)).when(globalSettingsRepository).findById(3);
    Mockito.doReturn(List.of(post)).when(postRepository).findAll();
    JsonNode jsonNode = userService.getAllStatistics();
    Assertions.assertEquals(2, jsonNode.get("viewsCount").asInt());
  }

  @Test
  void logout() {
    JsonNode jsonNode = userService.logout();
    Assertions.assertTrue(jsonNode.get("result").asBoolean());
  }


  @Test
  void getCaptcha() throws IOException {
    JsonNode jsonNode = userService.getCaptcha();
    Mockito.verify(captchaCodeRepository, Mockito.times(1))
        .save(ArgumentMatchers.isNotNull());
    Assertions.assertNotNull(jsonNode.get("secret").asText());
  }

  private Post newPost() {
    return new Post(1, 1, ModerationStatus.NEW, new User(1, 1, LocalDateTime.now(), "vanya",
        "some@mail.ru", "123456", "123456", "123.jpr", new Role(1)),
        new User(1, 1, LocalDateTime.now(), "vanya",
            "some@mail.ru", "123456", "123456", "123.jpr", new Role(1)),
        null, LocalDateTime.now(), "Hello World", "SSSSSSSSSSSSSSSSS", 2);
  }

  private User getUser() {
    User user = new User();
    user.setId(0);
    user.setEmail("some@mail.ru");
    user.setName("vanya");
    user.setRegTime(LocalDateTime.now());
    user.setPassword("123456");
    user.setCode("123456");
    return user;
  }
}