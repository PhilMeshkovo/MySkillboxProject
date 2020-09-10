package main.controller;

import com.fasterxml.jackson.databind.JsonNode;
import java.io.IOException;
import javax.persistence.EntityNotFoundException;
import main.dto.request.ChangePasswordRequest;
import main.dto.request.LoginRequest;
import main.dto.request.RegisterFormRequest;
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
  public ResponseEntity<?> addUser(@RequestBody RegisterFormRequest registerForm) {
    try {
      return ResponseEntity.ok(userService.saveUser(registerForm));
    } catch (EntityNotFoundException e) {
      return new ResponseEntity<>(e.getMessage(), HttpStatus.NOT_FOUND);
    }
  }

  @PostMapping("/auth/login")
  public ResponseEntity<?> login(
      @RequestBody LoginRequest loginDto) {
    return ResponseEntity.ok(userService.login(loginDto));
  }

  @GetMapping("/auth/check")
  public ResponseEntity<?> checkUser() {
    JsonNode object = userService.check();
    return ResponseEntity.ok(object);
  }

  @PostMapping("/auth/restore")
  public ResponseEntity<?> restore(
      @RequestBody JsonNode email) {
    return ResponseEntity.ok(userService.restore(email.get("email").asText()));
  }

  @PostMapping("/auth/password")
  public ResponseEntity<?> postNewPassword(
      @RequestBody ChangePasswordRequest changePasswordDto) {
    JsonNode jsonNode = userService.postNewPassword(changePasswordDto);
    if (jsonNode.has("error")) {
      return new ResponseEntity<>(jsonNode, HttpStatus.BAD_REQUEST);
    } else {
      return ResponseEntity.ok(jsonNode);
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
      return ResponseEntity.ok(jsonNode);
    } catch (Exception e) {
      return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
    }
  }

  @GetMapping("/statistics/my")
  public ResponseEntity<?> getMyStatistics() {
    try {
      JsonNode jsonNode = userService.getMyStatistics();
      return ResponseEntity.ok(jsonNode);
    } catch (Exception e) {
      return new ResponseEntity<>(e.getMessage(), HttpStatus.UNAUTHORIZED);
    }
  }

  @GetMapping("/statistics/all")
  public ResponseEntity<?> getAllStatistics() {
    try {
      JsonNode jsonNode = userService.getAllStatistics();
      return ResponseEntity.ok(jsonNode);
    } catch (Exception e) {
      return new ResponseEntity<>(e.getMessage(), HttpStatus.UNAUTHORIZED);
    }
  }

  @GetMapping("/auth/logout")
  public ResponseEntity<?> logout() {
    return ResponseEntity.ok(userService.logout());
  }

  @GetMapping("/auth/captcha")
  public ResponseEntity<?> getCaptcha() throws IOException {
    JsonNode jsonNode = userService.getCaptcha();
    return ResponseEntity.ok(jsonNode);
  }
}
