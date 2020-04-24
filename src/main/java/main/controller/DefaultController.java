package main.controller;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Date;
import main.model.Post;
import main.model.User;
import main.model.enums.ModerationStatus;
import main.repository.PostRepository;
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

  @RequestMapping("/")
  public String index() throws IOException {
//    User user = new User();
//    user.setEmail("ssssss@mail.ru");
//    user.setIsModerator(1);
//    user.setName("Ivan");
//    user.setPassword("3333");
//    user.setRegTime(new Date());
//    userRepository.save(user);
//
//    Post post = new Post();
//    post.setUser(user);
//    post.setTime(new Date());
//    post.setTitle("baab");
//    post.setText("ggggg");
//    post.setValue(5);
//    post.setModerationStatus(ModerationStatus.NEW);
//    repository.save(post);
//    System.out.println(post.getUser().getPassword());
    FileInputStream inFile = new FileInputStream(
        "src/main/resources/templates/index.html");
    byte[] str = new byte[inFile.available()];
    inFile.read(str);
    String text = new String(str);
    return text;
  }
}

