package main.controller;

import com.fasterxml.jackson.databind.JsonNode;
import java.io.IOException;
import javassist.NotFoundException;
import javax.persistence.EntityNotFoundException;
import main.dto.request.ChangePasswordRequest;
import main.dto.request.LoginRequest;
import main.dto.request.PostProfileRequest;
import main.dto.request.PostProfileRequestWithPhoto;
import main.dto.request.RegisterFormRequest;
import main.dto.response.ResultResponseWithErrors;
import main.exception.UnauthorizedException;
import main.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class ApiAuthController {

  @Autowired
  UserService userService;

  @PostMapping("/auth/register")
  @ResponseStatus(HttpStatus.OK)
  public ResponseEntity<?> addUser(@RequestBody RegisterFormRequest registerForm) {
      return ResponseEntity.ok(userService.saveUser(registerForm));
  }

  @PostMapping("/auth/login")
  @ResponseStatus(HttpStatus.OK)
  public ResponseEntity<?> login(
      @RequestBody LoginRequest loginDto) {
    return ResponseEntity.ok(userService.login(loginDto));
  }

  @GetMapping("/auth/check")
  @ResponseStatus(HttpStatus.OK)
  public ResponseEntity<?> checkUser() {
    return ResponseEntity.ok(userService.check());
  }

  @PostMapping("/auth/restore")
  @ResponseStatus(HttpStatus.OK)
  public ResponseEntity<?> restore(
      @RequestBody JsonNode email) {
    return ResponseEntity.ok(userService.restore(email.get("email").asText()));
  }

  @PostMapping("/auth/password")
  @ResponseStatus(HttpStatus.OK)
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
  @ResponseStatus(HttpStatus.OK)
  public ResponseEntity<?> postNewProfile(
      @RequestBody PostProfileRequest request
  ) {
      ResultResponseWithErrors response = userService.postNewProfile(request);
      return ResponseEntity.ok(response);
  }

  @GetMapping("/statistics/my")
  @ResponseStatus(HttpStatus.OK)
  public ResponseEntity<?> getMyStatistics() {
      JsonNode jsonNode = userService.getMyStatistics();
      return ResponseEntity.ok(jsonNode);
  }

  @GetMapping("/statistics/all")
  @ResponseStatus(HttpStatus.OK)
  public ResponseEntity<?> getAllStatistics() {
      JsonNode jsonNode = userService.getAllStatistics();
      return ResponseEntity.ok(jsonNode);
  }

  @GetMapping("/auth/logout")
  @ResponseStatus(HttpStatus.OK)
  public ResponseEntity<?> logout() {
    return ResponseEntity.ok(userService.logout());
  }

  @GetMapping("/auth/captcha")
  @ResponseStatus(HttpStatus.OK)
  public ResponseEntity<?> getCaptcha() throws IOException {
    JsonNode jsonNode = userService.getCaptcha();
    return ResponseEntity.ok(jsonNode);
  }

  @ExceptionHandler(NotFoundException.class)
  public String handleNotFoundException(NotFoundException e) {
    return e.getMessage();
  }
}
