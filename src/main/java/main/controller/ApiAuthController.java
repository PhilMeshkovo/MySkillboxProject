package main.controller;

import com.fasterxml.jackson.databind.JsonNode;
import java.io.IOException;
import javax.persistence.EntityNotFoundException;
import main.dto.request.ChangePasswordRequest;
import main.dto.request.LoginRequest;
import main.dto.request.PostProfileRequest;
import main.dto.request.PostProfileRequestWithPhoto;
import main.dto.request.RegisterFormRequest;
import main.dto.response.ResultResponseWithErrors;
import main.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
    return ResponseEntity.ok(userService.check());
  }

  @PostMapping("/auth/restore")
  public ResponseEntity<?> restore(
      @RequestBody JsonNode email) {
    return ResponseEntity.ok(userService.restore(email.get("email").asText()));
  }

  @PostMapping("/auth/password")
  public ResponseEntity<?> postNewPassword(
      @RequestBody ChangePasswordRequest changePasswordDto) {
    return ResponseEntity.ok(userService.postNewPassword(changePasswordDto));

  }

  @PostMapping(path = "/profile/my", consumes = {"multipart/form-data"})
  public ResponseEntity<?> postNewProfileWithPhoto(
      @ModelAttribute PostProfileRequestWithPhoto request
  ) {
    try {
      ResultResponseWithErrors response = userService.postNewProfileWithPhoto(request);
      return ResponseEntity.ok(response);
    } catch (Exception e) {
      return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
    }
  }

  @PostMapping(path = "/profile/my", consumes = {"application/json"})
  public ResponseEntity<?> postNewProfile(
      @RequestBody PostProfileRequest request
  ) {
    try {
      ResultResponseWithErrors response = userService.postNewProfile(request);
      return ResponseEntity.ok(response);
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
