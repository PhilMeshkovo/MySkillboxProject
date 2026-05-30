package main.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import javax.imageio.ImageIO;
import main.TestFixtures;
import main.dto.response.ResponseApiInit;
import main.model.User;
import main.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.acls.model.NotFoundException;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.util.ReflectionTestUtils;

class UtilityServiceTest {

  @Test
  void initServiceReturnsMetadataUploadsAndReadsImages() throws Exception {
    InitService initService = new InitService();
    ResponseApiInit init = initService.getInit();
    assertEquals("DevPub", init.getTitle());
    assertEquals("Блинов Филипп", init.getCopyright());

    byte[] imageBytes = jpegBytes();
    String uploadedPath = initService.uploadImage(new MockMultipartFile("image", "test.jpg", "image/jpeg", imageBytes));
    assertTrue(uploadedPath.startsWith("/upload/"));
    assertTrue(uploadedPath.endsWith(".jpg"));
    Path uploaded = Path.of(uploadedPath.substring(1));
    Files.deleteIfExists(uploaded);
    Files.deleteIfExists(uploaded.getParent());

    assertNotNull(initService.getImage("default-1.png"));
    assertThrows(javassist.NotFoundException.class, () -> initService.getImage("absent-file.jpg"));
  }

  @Test
  void mailSenderBuildsSimpleMailMessage() {
    JavaMailSender javaMailSender = mock(JavaMailSender.class);
    MailSender sender = new MailSender(javaMailSender);
    ReflectionTestUtils.setField(sender, "username", "from@test");

    sender.send("to@test", "Subject", "Text");

    verify(javaMailSender).send(argThat((SimpleMailMessage message) ->
        "from@test".equals(message.getFrom())
            && "to@test".equals(message.getTo()[0])
            && "Subject".equals(message.getSubject())
            && "Text".equals(message.getText())));
  }

  @Test
  void authenticationServiceReturnsCurrentUserForPrincipalTypes() {
    AuthenticationService service = new AuthenticationService();
    UserRepository repository = mock(UserRepository.class);
    ReflectionTestUtils.setField(service, "userRepository", repository);
    User user = TestFixtures.user(1, 0);
    when(repository.findByEmail("mail@test")).thenReturn(java.util.Optional.of(user));

    SecurityContextHolder.getContext().setAuthentication(new TestingAuthenticationToken("mail@test", "pwd"));
    assertEquals(user, service.getCurrentUser().orElseThrow());

    org.springframework.security.core.userdetails.User principal =
        new org.springframework.security.core.userdetails.User("mail@test", "pwd", java.util.List.of());
    SecurityContextHolder.getContext().setAuthentication(new TestingAuthenticationToken(principal, "pwd"));
    assertEquals(user, service.getCurrentUser().orElseThrow());

    SecurityContextHolder.clearContext();
    assertThrows(NotFoundException.class, service::getCurrentUser);
  }

  @Test
  void captchaImageIsGenerated() throws Exception {
    byte[] captcha = CaptchaCreateImage.getCaptcha("abc", "png");
    assertNotNull(captcha);
    assertTrue(captcha.length > 100);
  }

  private byte[] jpegBytes() throws Exception {
    BufferedImage image = new BufferedImage(2, 2, BufferedImage.TYPE_INT_RGB);
    ByteArrayOutputStream out = new ByteArrayOutputStream();
    ImageIO.write(image, "jpg", out);
    return out.toByteArray();
  }
}
