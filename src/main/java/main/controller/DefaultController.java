package main.controller;

import java.io.FileInputStream;
import java.io.IOException;
import main.repository.PostRepository;
import main.repository.TagRepository;
import main.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class DefaultController {

  @Autowired
  PostRepository repository;

  @Autowired
  UserRepository userRepository;

  @Autowired
  TagRepository tagRepository;

  @RequestMapping("/")
  public String index() throws IOException {
    FileInputStream inFile = new FileInputStream(
        "src/main/resources/templates/index.html");
    byte[] str = new byte[inFile.available()];
    inFile.read(str);
    String text = new String(str);
    return text;
  }
}

