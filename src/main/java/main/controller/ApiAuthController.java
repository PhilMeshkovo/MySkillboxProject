package main.controller;

import main.model.RegisterForm;
import main.repository.UserRepository;
import main.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
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

  @PostMapping("/login")
  public ResponseEntity<?> login(
      @RequestParam(value = "email")String email,
      @RequestParam(value = "password")String password){
    return new ResponseEntity<>("done", HttpStatus.OK);
  }
}
