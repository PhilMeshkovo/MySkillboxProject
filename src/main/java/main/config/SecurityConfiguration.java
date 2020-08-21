package main.config;

import lombok.extern.slf4j.Slf4j;
import main.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

@Slf4j
@EnableWebSecurity
public class SecurityConfiguration extends WebSecurityConfigurerAdapter {

  @Autowired
  UserService userService;

  private static final String[] AUTH_WHITELIST = {
      "/**",
      "/api/init",
      "/api/post/byDate",
      "/api/post/byTag",
      "/api/post/search",
      "/api/auth/register",
      "/api/auth/login",
      "/api/tag",
      "/api/calendar",
      "/api/auth/check",
      "/api/auth/restore",
      "/api/auth/password",
      "/api/auth/captcha",
      "/api/statistics/all"
  };

  @Override
  protected void configure(HttpSecurity http) throws Exception {
    http.csrf().disable()
        .authorizeRequests().antMatchers(AUTH_WHITELIST).permitAll()
        .and()
        .authorizeRequests().antMatchers(HttpMethod.GET, "/api/post").permitAll()
        .and()
        .authorizeRequests().antMatchers(HttpMethod.GET, "/api/settings/").permitAll()
        .and()
        .authorizeRequests().antMatchers(HttpMethod.GET, "/api/post/{id}").permitAll()
        .anyRequest().authenticated();
  }

  @Bean
  public PasswordEncoder bcryptPasswordEncoder() {
    return new BCryptPasswordEncoder();
  }

  @Override
  protected void configure(AuthenticationManagerBuilder auth) throws Exception {
    auth.userDetailsService(userService)
        .passwordEncoder(bcryptPasswordEncoder());
  }
}
