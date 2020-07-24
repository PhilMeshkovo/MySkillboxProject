package main.service;

import java.text.ParseException;
import main.dto.PostByIdApi;
import main.dto.PostListApi;
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
    PostListApi postListApi = postService.getAllPosts(0, 10, "BEST");
    Assertions.assertNotNull(postListApi);
  }

  @Test
  void getAllPostsByTextAndTitle() {
    PostListApi postListApi = postService.getAllPostsByTextAndTitle(0, 10, "on");
    Assertions.assertNotNull(postListApi);
  }

  @Test
  void getAllPostsByTag() {
    PostListApi postListApi = postService.getAllPostsByTag(0, 10, "postgres");
    Assertions.assertNotNull(postListApi);
  }

  @Test
  void getAllPostsByDate() throws ParseException {
    PostListApi postListApi = postService.getAllPostsByDate(0, 10, "1971-01-01");
    Assertions.assertNotNull(postListApi);
  }

  @Test
  void findPostById() {
    PostByIdApi postByIdApi = postService.findPostById(4);
    Assertions.assertEquals(4, postByIdApi.getId());
    Assertions.assertEquals("Ad voluptate laudantium.", postByIdApi.getTitle());
  }

  @Test
  void getAllPostsToModeration() {
    PostListApi postListApi = postService.getAllPostsToModeration(0,10,"NEW");
    Assertions.assertEquals(2, postListApi.getCount());
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