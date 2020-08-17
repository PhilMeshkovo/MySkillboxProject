package main.service;

import com.fasterxml.jackson.databind.JsonNode;
import java.time.LocalDateTime;
import java.util.Optional;
import main.dto.LoginDto;
import main.dto.RegisterForm;
import main.model.Role;
import main.model.User;
import main.repository.UserRepository;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
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
    RegisterForm registerForm = new RegisterForm();
    registerForm.setE_mail("tarakan@mail.ru");
    registerForm.setCaptcha("123456");
    registerForm.setCaptcha_secret("123456");
    registerForm.setName("vanya");
    registerForm.setPassword("12345");
    JsonNode jsonNode = userService.saveUser(registerForm);
    Assertions.assertFalse(jsonNode.get("result").asBoolean());
    Assertions.assertEquals("Пароль короче 6-ти символов",
        jsonNode.get("errors").get("password").asText());
  }

  @Test
  void login() throws Exception {
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
  }

  @Test
  void postNewPassword() {
  }

  @Test
  void postNewProfile() {
  }

  @Test
  void getMyStatistics() throws Exception {
    Mockito.doReturn(new User(1, 1, LocalDateTime.now(), "vanya",
        "some@mail.ru", "123456", "123456", "123.jpr", new Role(1)))
        .when(authenticationService).getCurrentUser();
    JsonNode jsonNode = userService.getMyStatistics();
    Assertions.assertEquals(0, jsonNode.get("postsCount").asInt());
  }

  @Test
  void getAllStatistics() {
  }

  @Test
  void logout() {
  }

  @Test
  void getSettings() {
  }

  @Test
  void putSettings() {
  }

  @Test
  void getCaptcha() {
  }

  @Test
  void createCaptchaValue() {
  }
}