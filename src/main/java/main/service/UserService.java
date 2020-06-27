package main.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Optional;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import main.model.RegisterForm;
import main.model.Role;
import main.model.User;
import main.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.security.acls.model.NotFoundException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class UserService implements UserDetailsService {

  @Autowired
  UserRepository userRepository;


  @Override
  public UserDetails loadUserByUsername(@NonNull String username)
      throws UsernameNotFoundException {
    User user = userRepository.findByEmail(username)
        .orElseThrow(() -> new UsernameNotFoundException("user " + username + " not found!"));
    return new org.springframework.security.core.userdetails.User(user.getEmail(),
        user.getPassword()
        , new ArrayList<>());
  }

  @Bean
  public PasswordEncoder passwordEncoder() {
    return new BCryptPasswordEncoder();
  }

  public boolean saveUser(RegisterForm registerFormUser) {
    Optional<User> byEmail = userRepository.findByEmail(registerFormUser.getEmail());
    System.out.println(registerFormUser.toString());
    if (!byEmail.isEmpty()) {
      return false;
    }
    User user = new User();
    user.setEmail(registerFormUser.getEmail());
    user.setName(registerFormUser.getName());
    user.setRole(new Role(1, "ROLE_USER"));
    user.setRegTime(LocalDateTime.now());
    user.setPassword(passwordEncoder().encode(registerFormUser.getPassword()));
    userRepository.save(user);
    log.info("saved user");
    return true;
  }

  public User getCurrentUser() throws Exception {
    Authentication auth = SecurityContextHolder.getContext().getAuthentication();

    if (null == auth) {
      throw new NotFoundException("");
    }

    Object obj = auth.getPrincipal();
    String username = "";

    if (obj instanceof UserDetails) {
      username = ((UserDetails) obj).getUsername();
    } else {
      username = obj.toString();
    }

    User us = userRepository.findByEmail(username).get();
    return us;
  }

  public JsonNode login(String email, String password) {
    ObjectMapper mapper = new ObjectMapper();
    ObjectNode object = mapper.createObjectNode();
    ObjectNode objectUser = mapper.createObjectNode();
    Optional<User> userByEmail = userRepository.findByEmail(email);
    if (!userByEmail.isEmpty() && passwordEncoder()
        .matches(password, userByEmail.get().getPassword())) {
      objectUser.put("id", userByEmail.get().getId());
      objectUser.put("name", userByEmail.get().getName());
      objectUser.put("email", userByEmail.get().getEmail());
      objectUser.put("moderation", true);
      objectUser.put("moderationCount", 21);
      objectUser.put("settings", true);
      object.put("result", true);
      object.put("user", objectUser);
    } else {
      object.put("result", false);
    }
    System.out.println(passwordEncoder()
        .matches(password, userByEmail.get().getPassword()));
    return object;
  }
}
