package main.controller;

import com.fasterxml.jackson.databind.JsonNode;
import main.model.RegisterForm;
import main.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class ApiAuthController {

  @Autowired
  UserService userService;

  @PostMapping("/register")
  public boolean addUser(@RequestBody RegisterForm registerForm) {
    return userService.saveUser(registerForm);
  }

  @PostMapping("/login")
  public ResponseEntity<?> login(
      @RequestParam(value = "email") String email,
      @RequestParam(value = "password") String password) {
    JsonNode object = userService.login(email, password);
    return new ResponseEntity<>(object, HttpStatus.OK);
  }

  @GetMapping("/check")
  public ResponseEntity<?> checkUser() {
    JsonNode object = userService.check();
    return new ResponseEntity<>(object, HttpStatus.OK);
  }

  @PostMapping("/restore")
  public ResponseEntity<?> restore(
      @RequestParam(value = "email", required = false) String email) {
    JsonNode jsonNode = userService.restore(email);
    return new ResponseEntity<>(jsonNode, HttpStatus.OK);
  }
}
