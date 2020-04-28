package main.controller;

import main.api.response.ResponseApiInit;
import main.service.InitService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class ApiGeneralController {

  @Autowired
  InitService initService;

  @GetMapping("/init")
  public ResponseApiInit getInit() {
    return initService.getInit();
  }
}
