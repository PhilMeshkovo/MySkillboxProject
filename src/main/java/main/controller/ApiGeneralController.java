package main.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import java.io.IOException;
import main.dto.request.GlobalSettingsRequest;
import main.dto.response.ResponseApiInit;
import main.service.InitService;
import main.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api")
public class ApiGeneralController {

  @Autowired
  InitService initService;

  @Autowired
  UserService userService;

  @Autowired
  ObjectMapper mapper;

  public void before() {
    mapper.disable(SerializationFeature.FAIL_ON_EMPTY_BEANS);
  }

  @GetMapping("/init")
  public ResponseApiInit getInit() {
    return initService.getInit();
  }

  @PostMapping("/image")
  public ResponseEntity<?> upload(@RequestBody MultipartFile image) {
    try {
      return ResponseEntity.ok(initService.uploadImage(image));
    } catch (IOException e) {
      return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
    }
  }

  @GetMapping(value = "/image/{link}", produces = MediaType.IMAGE_JPEG_VALUE)
  public @ResponseBody
  byte[] getImage(
      @PathVariable String link) {
    before();
    byte[] image = new byte[0];
    try {
      image = initService.getImage(link);
    } catch (Exception e) {
      e.getMessage();
    }
    return image;
  }

  @GetMapping("/settings")
  public ResponseEntity<?> getSettings() {
    return ResponseEntity.ok(userService.getSettings());
  }

  @PutMapping("/settings")
  public void putSettings(
      @RequestBody GlobalSettingsRequest globalSettingsDto
  )
      throws Exception {
    userService.putSettings(globalSettingsDto);
  }
}
