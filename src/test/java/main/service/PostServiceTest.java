package main.service;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;


@RunWith(SpringRunner.class)
@SpringBootTest
class PostServiceTest {

  @Autowired
  private PostService postService;

  @Test
  void getAllPosts() {

    var best = postService.getAllPosts(0, 10, "BEST");
    Assertions.assertNotNull(best);
  }

  @Test
  void getAllPostsByTextAndTitle() {
  }

  @Test
  void getAllPostsByTag() {
  }

  @Test
  void getAllPostsByDate() {
  }

  @Test
  void findPostById() {
  }

  @Test
  void getAllMyPosts() {
  }

  @Test
  void getAllPostsToModeration() {
  }

  @Test
  void addPost() {
  }

  @Test
  void updatePost() {
  }

  @Test
  void addCommentToPost() {
  }

  @Test
  void getTag() {
  }

  @Test
  void moderationPost() {
  }

  @Test
  void getAllPostsInYear() {
  }

  @Test
  void postLike() {
  }

  @Test
  void postDislike() {
  }
}