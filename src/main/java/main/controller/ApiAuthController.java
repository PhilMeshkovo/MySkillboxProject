package main.controller;

import com.fasterxml.jackson.databind.JsonNode;
import java.io.IOException;
import main.dto.LoginDto;
import main.dto.RegisterForm;
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
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api")
public class ApiAuthController {

  @Autowired
  UserService userService;

  @PostMapping("/auth/register")
  public ResponseEntity<?> addUser(@RequestBody RegisterForm registerForm) {
    JsonNode jsonNode = userService.saveUser(registerForm);
    return new ResponseEntity<>(jsonNode, HttpStatus.OK);
  }

  @PostMapping("/auth/login")
  public ResponseEntity<?> login(
      @RequestBody LoginDto loginDto) {
    JsonNode object = userService.login(loginDto);
    return new ResponseEntity<>(object, HttpStatus.OK);
  }

  @GetMapping("/auth/check")
  public ResponseEntity<?> checkUser() {
    JsonNode object = userService.check();
    return new ResponseEntity<>(object, HttpStatus.OK);
  }

  @PostMapping("/auth/restore")
  public ResponseEntity<?> restore(
      @RequestBody JsonNode email) {
    JsonNode jsonNode = userService.restore(email.get("email").asText());
    return new ResponseEntity<>(jsonNode, HttpStatus.OK);
  }

  @PostMapping("/auth/password")
  public ResponseEntity<?> postNewPassword(
      @RequestParam(value = "code", required = false) String code,
      @RequestParam(value = "password") String password,
      @RequestParam(value = "captcha") String captcha,
      @RequestParam(value = "captcha_secret") String captcha_secret) {
    JsonNode jsonNode = userService.postNewPassword(code, password, captcha, captcha_secret);
    if (jsonNode.has("error")) {
      return new ResponseEntity<>(jsonNode, HttpStatus.BAD_REQUEST);
    } else {
      return new ResponseEntity<>(jsonNode, HttpStatus.OK);
    }
  }

  @PostMapping(path = "/profile/my", consumes = {"multipart/form-data"})
  public ResponseEntity<?> postNewProfile(
      @RequestParam(value = "photo", required = false) MultipartFile photo,
      @RequestParam(value = "email", required = false) String email,
      @RequestParam(value = "password", required = false) String password,
      @RequestParam(value = "removePhoto", required = false) Integer removePhoto,
      @RequestParam(value = "name", required = false) String name) {
    try {
      JsonNode jsonNode = userService.postNewProfile(photo, name,
          email, password, removePhoto);
      return new ResponseEntity<>(jsonNode, HttpStatus.OK);
    } catch (Exception e) {
      return new ResponseEntity<>(e.getMessage(), HttpStatus.OK);
    }
  }

  @GetMapping("/statistics/my")
  public ResponseEntity<?> getMyStatistics() {
    try {
      JsonNode jsonNode = userService.getMyStatistics();
      return new ResponseEntity<>(jsonNode, HttpStatus.OK);
    } catch (Exception e) {
      return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
    }
  }

  @GetMapping("/statistics/all")
  public ResponseEntity<?> getAllStatistics() {
    try {
      JsonNode jsonNode = userService.getAllStatistics();
      return new ResponseEntity<>(jsonNode, HttpStatus.OK);
    } catch (Exception e) {
      return new ResponseEntity<>(e.getMessage(), HttpStatus.UNAUTHORIZED);
    }
  }

  @GetMapping("/auth/logout")
  public ResponseEntity<?> logout() {
    JsonNode jsonNode = userService.logout();
    return new ResponseEntity<>(jsonNode, HttpStatus.OK);
  }

  @GetMapping("/auth/captcha")
  public ResponseEntity<?> getCaptcha() throws IOException {
    JsonNode jsonNode = userService.getCaptcha();
    return new ResponseEntity<>(jsonNode, HttpStatus.OK);
  }
}
