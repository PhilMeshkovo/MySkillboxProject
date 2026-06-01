package main.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import com.fasterxml.jackson.databind.JsonNode;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import jakarta.persistence.EntityNotFoundException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import main.TestFixtures;
import main.config.SecurityConfiguration;
import main.dto.request.ChangePasswordRequest;
import main.dto.request.GlobalSettingsRequest;
import main.dto.request.LoginRequest;
import main.dto.request.PostProfileRequest;
import main.dto.request.RegisterFormRequest;
import main.dto.response.ResponsePostApi;
import main.dto.response.ResultResponseWithErrors;
import main.mapper.PostMapper;
import main.model.CaptchaCode;
import main.model.GlobalSettings;
import main.model.Post;
import main.model.User;
import main.model.enums.ModerationStatus;
import main.repository.CaptchaCodeRepository;
import main.repository.GlobalSettingsRepository;
import main.repository.PostRepository;
import main.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class UserServiceUnitTest {

  @Mock UserRepository userRepository;
  @Mock SecurityConfiguration securityConfiguration;
  @Mock GlobalSettingsRepository globalSettingsRepository;
  @Mock PostMapper postMapper;
  @Mock CaptchaCodeRepository captchaCodeRepository;
  @Mock HttpServletRequest request;
  @Mock HttpSession session;
  @Mock MailSender mailSender;
  @Mock PostRepository postRepository;
  @Mock AuthenticationService authenticationService;

  UserService service;
  User user;
  Map<String, Integer> authorizedUsers;
  BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

  @BeforeEach
  void setUp() {
    service = new UserService(userRepository, securityConfiguration, globalSettingsRepository, postMapper,
        captchaCodeRepository, request, mailSender, postRepository, authenticationService);
    ReflectionTestUtils.setField(service, "minPassword", 6);
    ReflectionTestUtils.setField(service, "maxName", 20);
    ReflectionTestUtils.setField(service, "minName", 2);
    user = TestFixtures.user(7, 1);
    user.setPassword(encoder.encode("secret12"));
    authorizedUsers = new HashMap<>();
    authorizedUsers.put("session", user.getId());
    lenient().when(request.getSession()).thenReturn(session);
    lenient().when(session.getId()).thenReturn("session");
    lenient().when(authenticationService.getAuthorizedUsers()).thenReturn(authorizedUsers);
    lenient().when(securityConfiguration.bcryptPasswordEncoder()).thenReturn(encoder);
  }

  @Test
  void loadsUserDetailsOrThrows() {
    when(userRepository.findByEmail(user.getEmail())).thenReturn(Optional.of(user));

    assertEquals(user.getEmail(), service.loadUserByUsername(user.getEmail()).getUsername());
    assertThrows(UsernameNotFoundException.class, () -> service.loadUserByUsername("missing@test"));
  }

  @Test
  void saveUserHandlesSuccessDisabledModeAndValidationErrors() {
    when(globalSettingsRepository.findByCode("MULTIUSER_MODE")).thenReturn(Optional.of(setting("YES")));
    RegisterFormRequest request = register("new@test", "GoodName", "secret12");
    when(userRepository.findByEmail("new@test")).thenReturn(Optional.empty());
    assertTrue(service.saveUser(request).isResult());
    verify(userRepository).save(argThat(saved -> saved.getEmail().equals("new@test")));

    RegisterFormRequest invalid = register(user.getEmail(), "x", "123");
    when(userRepository.findByEmail(user.getEmail())).thenReturn(Optional.of(user));
    ResultResponseWithErrors errors = service.saveUser(invalid);
    assertFalse(errors.isResult());
    assertEquals("Этот e-mail уже зарегистрирован", errors.getErrors().getEmail());
    assertEquals("Пароль короче 6-ти символов", errors.getErrors().getPassword());
    assertEquals("Имя указано неверно", errors.getErrors().getName());

    when(globalSettingsRepository.findByCode("MULTIUSER_MODE")).thenReturn(Optional.of(setting("NO")));
    assertFalse(service.saveUser(request).isResult());
  }

  @Test
  void loginCheckRestoreLogoutAndCaptchaWork() throws Exception {
    when(userRepository.findByEmail(user.getEmail())).thenReturn(Optional.of(user));
    when(postRepository.count()).thenReturn(2L);
    when(postRepository.findAllPostsToModeration(0, 2, "NEW")).thenReturn(List.of());

    assertTrue(service.login(new LoginRequest(user.getEmail(), "secret12")).isResult());
    verify(authenticationService, atLeastOnce()).setAuthorizedUsers(anyMap());
    assertFalse(service.login(new LoginRequest(user.getEmail(), "wrong")).isResult());

    when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));
    assertTrue(service.check().isResult());
    user.setIsModerator(0);
    assertFalse(service.check().getUser().isModeration());
    authorizedUsers.clear();
    assertFalse(service.check().isResult());
    authorizedUsers.put("session", user.getId());

    assertTrue(service.restore(user.getEmail()).isResult());
    verify(mailSender).send(eq(user.getEmail()), eq("Code"), contains(user.getCode()));

    assertTrue(service.logout().isResult());

    JsonNode captcha = service.getCaptcha();
    assertTrue(captcha.get("secret").asText().length() >= 22);
    assertTrue(captcha.get("image").asText().startsWith("data:image/png;base64,"));
    verify(captchaCodeRepository).save(any(CaptchaCode.class));
    verify(captchaCodeRepository).deleteByTimeBefore(any(LocalDateTime.class));
  }

  @Test
  void passwordResetHandlesSuccessAndAllErrorTypes() {
    CaptchaCode captcha = CaptchaCode.builder().code("abc").secretCode("secret").build();
    when(userRepository.findByCode("code")).thenReturn(Optional.of(user));
    when(captchaCodeRepository.findByCode("abc")).thenReturn(Optional.of(captcha));
    when(userRepository.getOne(user.getId())).thenReturn(user);
    assertTrue(service.postNewPassword(new ChangePasswordRequest("code", "secret12", "abc", "secret")).isResult());

    when(userRepository.findByCode("bad")).thenReturn(Optional.empty());
    when(captchaCodeRepository.findByCode("missing")).thenReturn(Optional.empty());
    ResultResponseWithErrors errors = service.postNewPassword(new ChangePasswordRequest("bad", "1", "missing", "wrong"));
    assertFalse(errors.isResult());
    assertNotNull(errors.getErrors().getCode());
    assertEquals("Пароль короче 6-ти символов", errors.getErrors().getPassword());
    assertEquals("Код с картинки введён неверно", errors.getErrors().getCaptcha());
  }

  @Test
  void profileUpdatesNamePasswordRemovePhotoAndValidationErrors() {
    when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));
    when(userRepository.findByEmail(user.getEmail())).thenReturn(Optional.of(user));
    when(userRepository.getOne(user.getId())).thenReturn(user);

    assertTrue(service.postNewProfile(new PostProfileRequest(user.getEmail(), "NewName")).isResult());
    assertEquals("NewName", user.getName());
    assertTrue(service.postNewProfile(new PostProfileRequest(user.getEmail(), "NewName", "secret12")).isResult());
    assertTrue(encoder.matches("secret12", user.getPassword()));
    assertTrue(service.postNewProfile(new PostProfileRequest("", user.getEmail(), "NewName", 1)).isResult());
    assertNull(user.getPhoto());

    User duplicate = TestFixtures.user(99, 0);
    duplicate.setEmail("taken@test");
    when(userRepository.findByEmail("taken@test")).thenReturn(Optional.of(duplicate));
    ResultResponseWithErrors errors = service.postNewProfile(
        new PostProfileRequest(null, "taken@test", "", "bad", null));
    assertFalse(errors.isResult());
    assertEquals("Этот e-mail уже зарегистрирован", errors.getErrors().getEmail());
    assertEquals("Имя указано неверно", errors.getErrors().getName());
    assertEquals("Пароль короче 6-ти символов", errors.getErrors().getPassword());
  }

  @Test
  void statisticsSettingsAndCaptchaValueAreCovered() throws Exception {
    when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));
    Post post = TestFixtures.post(1, user, ModerationStatus.ACCEPTED);
    ResponsePostApi response = TestFixtures.responsePostApi(1);
    when(postMapper.postToResponsePostApi(post)).thenReturn(response);
    when(postRepository.count()).thenReturn(1L);
    when(postRepository.findAllMyPosts(0, 1, "ACCEPTED", user.getId())).thenReturn(List.of(post));
    when(postRepository.findFirstMyPublication(user.getId())).thenReturn(TestFixtures.POST_TIME);
    assertEquals(1, service.getMyStatistics().get("postsCount").asInt());

    when(globalSettingsRepository.findByCode("STATISTICS_IS_PUBLIC")).thenReturn(Optional.of(setting("YES")));
    when(postRepository.findAll()).thenReturn(List.of(post));
    when(postRepository.findFirstPublication()).thenReturn(TestFixtures.POST_TIME);
    assertEquals(1, service.getAllStatistics().get("postsCount").asInt());

    when(globalSettingsRepository.findByCode("STATISTICS_IS_PUBLIC")).thenReturn(Optional.of(setting("NO")));
    user.setIsModerator(1);
    assertEquals(1, service.getAllStatistics().get("postsCount").asInt());
    user.setIsModerator(0);
    assertThrows(EntityNotFoundException.class, () -> service.getAllStatistics());

    GlobalSettings yes = setting("YES");
    yes.setCode("A");
    GlobalSettings no = setting("NO");
    no.setCode("B");
    when(globalSettingsRepository.findAll()).thenReturn(List.of(yes, no));
    assertTrue(service.getSettings().get("A").asBoolean());
    assertFalse(service.getSettings().get("B").asBoolean());

    user.setIsModerator(1);
    GlobalSettings first = setting("NO");
    GlobalSettings second = setting("YES");
    GlobalSettings third = setting("NO");
    when(globalSettingsRepository.getOne(1)).thenReturn(first);
    when(globalSettingsRepository.getOne(2)).thenReturn(second);
    when(globalSettingsRepository.getOne(3)).thenReturn(third);
    GlobalSettingsRequest settingsRequest = new GlobalSettingsRequest();
    settingsRequest.setMULTIUSER_MODE(true);
    settingsRequest.setPOST_PREMODERATION(false);
    settingsRequest.setSTATISTICS_IS_PUBLIC(true);
    service.putSettings(settingsRequest);
    assertEquals("YES", first.getValue());
    assertEquals("NO", second.getValue());
    assertEquals("YES", third.getValue());

    user.setIsModerator(0);
    assertThrows(Exception.class, () -> service.putSettings(settingsRequest));

    String captchaValue = UserService.createCaptchaValue(3);
    assertTrue(captchaValue.length() == 3 || captchaValue.length() == 4);
    assertTrue(captchaValue.matches("[A-Za-z0-9]+"));
  }

  private RegisterFormRequest register(String email, String name, String password) {
    RegisterFormRequest request = new RegisterFormRequest();
    request.setEmail(email);
    request.setName(name);
    request.setPassword(password);
    request.setCaptcha("captcha");
    request.setCaptchaSecret("secret");
    return request;
  }

  private GlobalSettings setting(String value) {
    GlobalSettings setting = new GlobalSettings();
    setting.setValue(value);
    setting.setCode("CODE");
    setting.setName("Name");
    return setting;
  }
}
