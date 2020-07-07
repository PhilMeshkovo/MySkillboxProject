package main.controller;

import com.fasterxml.jackson.databind.JsonNode;
import java.io.IOException;
import main.api.response.ResponseApiInit;
import main.service.InitService;
import main.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api")
public class ApiGeneralController {

  @Autowired
  InitService initService;

  @Autowired
  UserService userService;

  @GetMapping("/init")
  public ResponseApiInit getInit() {
    return initService.getInit();
  }

  @PostMapping("/image")
  @ResponseStatus(HttpStatus.OK)
  public String upload(@RequestBody MultipartFile image) {
    try {
      String answer = initService.uploadImage(image);
      return answer;
    } catch (IOException e) {
      return e.getMessage();
    }
  }

  @GetMapping("/settings")
  public ResponseEntity<?> getSettings() {
    JsonNode jsonNode = userService.getSettings();
    return new ResponseEntity<>(jsonNode, HttpStatus.OK);
  }

  @PutMapping("/settings")
  public void putSettings(
      @RequestParam(value = "MULTIUSER_MODE", required = false) boolean multiuserMode,
      @RequestParam(value = "POST_PREMODERATION", required = false) boolean postPremoderation,
      @RequestParam(value = "STATISTICS_IS_PUBLIC", required = false) boolean statisticsIsPublic)
      throws Exception {
    userService.putSettings(multiuserMode, postPremoderation, statisticsIsPublic);
  }
}
