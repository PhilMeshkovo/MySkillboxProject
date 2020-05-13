package main.config;

import java.util.List;
import javax.annotation.PostConstruct;
import lombok.NonNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class UserDetailServiceImpl implements UserDetailsService {

  @Autowired
  UserDetailsStorage userStorage;

  @PostConstruct
  public void init() {
    if (!userStorage.findByUsername("user").isPresent()) {
      userStorage.userDetailsList.add(UserDetailsImpl.builder()
          .username("user")
          .password(new BCryptPasswordEncoder().encode("password"))
          .authorities(List.of(Role.USER))
          .isAccountNonExpired(true)
          .isAccountNonLocked(true)
          .isCredentialsNonExpired(true)
          .enabled(true)
          .build());
    }
    if (!userStorage.findByUsername("admin").isPresent()) {
      userStorage.userDetailsList.add(UserDetailsImpl.builder()
          .username("admin")
          .password(new BCryptPasswordEncoder().encode("admin"))
          .authorities(List.of(Role.ADMIN))
          .isAccountNonExpired(true)
          .isAccountNonLocked(true)
          .isCredentialsNonExpired(true)
          .enabled(true)
          .build());
    }
  }

  @Override
  public UserDetailsImpl loadUserByUsername(@NonNull String username)
      throws UsernameNotFoundException {
    return userStorage.findByUsername(username)
        .orElseThrow(() -> new UsernameNotFoundException("user " + username + " not found!"));
  }
}
