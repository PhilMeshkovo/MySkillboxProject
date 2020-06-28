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
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

@Slf4j
@EnableWebSecurity
public class SecurityConfiguration extends WebSecurityConfigurerAdapter {

  @Autowired
  private UserService userService;

  private static final String[] AUTH_WHITELIST = {
      "/api/post",
      "/api/post/{id}",
      "/api/post/byDate",
      "/api/post/byTag",
      "/api/post/search",
      "/api/auth/register",
      "/api/image",
      "/api/auth/login",
      "/api/tag"
  };
  private static final String[] AUTH_BLACKLIST = {
      "/api/post/moderation",
      "/api/post/my"
  };

  @Override
  protected void configure(HttpSecurity http) throws Exception {
    http.cors().disable().csrf().disable()
        .authorizeRequests().antMatchers(AUTH_BLACKLIST).authenticated()
        .and()
        .authorizeRequests().antMatchers(HttpMethod.POST, "/api/post").authenticated()
        .and()
        .authorizeRequests().antMatchers(HttpMethod.POST, "/api/auth/login").permitAll()
        .and()
        .authorizeRequests().antMatchers(AUTH_WHITELIST).permitAll();
    http.authorizeRequests().anyRequest().authenticated()
        .and()
        .formLogin().and()
        .logout().logoutRequestMatcher(new AntPathRequestMatcher("/logout"))
        .permitAll();
//    http.csrf().disable()
//        .authorizeRequests()
//        .antMatchers("/**", "/api/post/**","/api/auth/login").permitAll()   // маска /** открывает доступ ко всему
//        .anyRequest()
//        .authenticated();

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
