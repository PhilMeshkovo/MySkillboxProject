package main.controller;

import main.model.RegisterForm;
import main.repository.UserRepository;
import main.service.UserService;
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

  @Autowired
  UserService userService;

  @PostMapping("/register")
  public boolean addUser(@RequestBody RegisterForm registerForm) {
    return userService.saveUser(registerForm);
  }
}
