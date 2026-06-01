package main.config;

import lombok.extern.slf4j.Slf4j;
import main.service.UserService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Slf4j
@Configuration
@EnableWebSecurity
public class SecurityConfiguration {

  private final UserService userService;

  private static final String[] AUTH_WHITELIST = {
      "/**",
      "/api/init",
      "/api/post/byDate",
      "/api/post/byTag",
      "/api/post/search",
      "/api/auth/register/",
      "/api/auth/login",
      "/api/tag",
      "/api/calendar",
      "/api/auth/check",
      "/api/auth/restore",
      "/api/auth/password",
      "/api/auth/captcha",
      "/api/statistics/all"
  };

  public SecurityConfiguration(UserService userService) {
    this.userService = userService;
  }

  @Bean
  public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
    http
        .csrf(csrf -> csrf.disable())
        .authorizeHttpRequests(auth -> auth
            .requestMatchers(AUTH_WHITELIST).permitAll()
            .requestMatchers(HttpMethod.GET, "/api/post").permitAll()
            .requestMatchers(HttpMethod.GET, "/api/settings/").permitAll()
            .requestMatchers(HttpMethod.GET, "/api/post/{id}").permitAll()
            .anyRequest().authenticated())
        .formLogin(formLogin -> formLogin.disable());
    return http.build();
  }

  @Bean
  public PasswordEncoder bcryptPasswordEncoder() {
    return new BCryptPasswordEncoder();
  }

  @Bean
  public DaoAuthenticationProvider authenticationProvider(PasswordEncoder passwordEncoder) {
    DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider(userService);
    authProvider.setPasswordEncoder(passwordEncoder);
    return authProvider;
  }
}
