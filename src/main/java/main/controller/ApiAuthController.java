package main.controller;

import main.model.User;
import main.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class ApiAuthController {

  @Autowired
  UserRepository userRepository;

  @PostMapping("/login")
  public Object getUser(@RequestBody User user) {
    return user.getPassword();
  }
}