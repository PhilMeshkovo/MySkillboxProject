package main.config;

import static org.junit.jupiter.api.Assertions.*;

import main.service.UserService;
import org.junit.jupiter.api.Test;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.context.support.StaticApplicationContext;
import org.springframework.mock.web.MockServletContext;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;

class ConfigUnitTest {

  @Test
  void mailConfigBuildsConfiguredSenderAndSecurityProvidesEncoder() {
    MailConfig mailConfig = new MailConfig();
    ReflectionTestUtils.setField(mailConfig, "host", "smtp.test");
    ReflectionTestUtils.setField(mailConfig, "username", "user@test");
    ReflectionTestUtils.setField(mailConfig, "password", "secret");
    ReflectionTestUtils.setField(mailConfig, "port", 2525);
    ReflectionTestUtils.setField(mailConfig, "protocol", "smtp");
    ReflectionTestUtils.setField(mailConfig, "debug", "true");

    JavaMailSenderImpl sender = (JavaMailSenderImpl) mailConfig.getMailSender();
    assertEquals("smtp.test", sender.getHost());
    assertEquals(2525, sender.getPort());
    assertEquals("smtp", sender.getJavaMailProperties().getProperty("mail.transport.protocol"));
    assertEquals("true", sender.getJavaMailProperties().getProperty("mail.debug"));

    PasswordEncoder encoder = new SecurityConfiguration(null).bcryptPasswordEncoder();
    assertTrue(encoder.matches("password", encoder.encode("password")));
  }

  @Test
  void mvcConfigRegistersRoutesWithoutThrowing() {
    MvcConfig mvcConfig = new MvcConfig();
    mvcConfig.addViewControllers(new ViewControllerRegistry(new StaticApplicationContext()));
    mvcConfig.addResourceHandlers(new ResourceHandlerRegistry(new StaticApplicationContext(), new MockServletContext()));
  }
}
