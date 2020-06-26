package main.controller;

import java.io.IOException;
import main.api.response.ResponseApiInit;
import main.service.InitService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api")
public class ApiGeneralController {

  @Autowired
  InitService initService;

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
}
