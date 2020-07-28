package main.service;

import com.fasterxml.jackson.databind.JsonNode;
import java.text.ParseException;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import main.dto.ListTagsDto;
import main.dto.PostByIdApi;
import main.dto.PostListApi;
import main.model.Post;
import main.model.PostComment;
import main.model.Role;
import main.model.Tag;
import main.model.User;
import main.model.enums.ModerationStatus;
import main.repository.PostCommentRepository;
import main.repository.PostRepository;
import main.repository.TagRepository;
import main.repository.UserRepository;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Bean;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.transaction.annotation.Transactional;


@RunWith(SpringRunner.class)
@SpringBootTest
class PostServiceTest {

  @Autowired
  private PostService postService;

  @Autowired
  private PostRepository postRepository;

  @Autowired
  private TagRepository tagRepository;

  @Autowired
  UserRepository userRepository;

  @Autowired
  PostCommentRepository postCommentRepository;

  @Bean
  public PasswordEncoder passwordEncoder() {
    return new BCryptPasswordEncoder();
  }

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
    PostByIdApi postByIdApi = postService.findPostById(8);
    Assertions.assertEquals(8, postByIdApi.getId());
    Assertions.assertEquals("Rerum eos quam.", postByIdApi.getTitle());
  }

  @Test
  void getAllPostsToModeration() {
    PostListApi postListApi = postService.getAllPostsToModeration(0, 10, "NEW");
    Assertions.assertEquals(3, postListApi.getCount());
  }

  @Test
  void addPost() throws Exception {
    Post post = Post.builder()
        .user(getUser())
        .moderationStatus(ModerationStatus.NEW)
        .title("hello world again!")
        .isActive(1)
        .tags(getTags("java,php"))
        .moderator(getUser())
        .text(getText())
        .time(LocalDateTime.now())
        .view_count(0)
        .build();
    Post newPost = postRepository.save(post);
    Assertions.assertEquals("hello world again!", newPost.getTitle());
    postRepository.delete(newPost);
  }

  @Test
  @Transactional
  void updatePost() {
    Post post = Post.builder()
        .user(getUser())
        .moderationStatus(ModerationStatus.NEW)
        .title("hello world again!")
        .isActive(1)
        .tags(getTags("java,php"))
        .moderator(getUser())
        .text(getText())
        .time(LocalDateTime.now())
        .view_count(0)
        .build();
    Post newPost = postRepository.save(post);
    Post postToUpdate = postRepository.getOne(newPost.getId());
    postToUpdate.setTitle("New hello world again!");
    Assertions.assertEquals("New hello world again!", postToUpdate.getTitle());
    postRepository.delete(newPost);
  }

  @Test
  void addCommentToPost() {
    User user = userRepository.findById(1).get();
    PostComment postComment = PostComment.builder()
        .post(getPost())
        .user(getUser())
        .time(LocalDateTime.now())
        .text("blablablabla")
        .build();
    PostComment savedPostComment = postCommentRepository.save(postComment);
    Assertions.assertEquals("blablablabla", postCommentRepository.findById(savedPostComment.getId())
        .get().getText());
    postCommentRepository.delete(savedPostComment);
  }

  @Test
  void getTag() {
    ListTagsDto listTagsDto = postService.getTag("postgres");
    Assertions.assertEquals(1.0, listTagsDto.getTags().get(0).getWeight());
  }

  @Test
  void getAllPostsInYear() {
    JsonNode jsonNode = postService.getAllPostsInYear("");
    Assertions.assertEquals(1, jsonNode.get("posts").size());
  }

  private String getText() {
    return "qqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqq"
        + "qqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqq"
        + "qqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqq"
        + "qqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqq"
        + "qqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqq"
        + "qqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqq"
        + "qqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqq"
        + "qqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqq"
        + "qqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqq";
  }

  private User getUser() {
    User user = new User();
    user.setEmail("tata@mail.ru");
    user.setName("tata tata");
    user.setRole(new Role(1, "ROLE_USER"));
    user.setRegTime(LocalDateTime.now());
    user.setPassword(passwordEncoder().encode("qwertyui"));
    user.setCode(UUID.randomUUID().toString());
    return user;
  }

  private Set<Tag> getTags(String tags) {
    String[] arrayTags = tags.split(",");
    Set<Tag> setTags = Arrays.stream(arrayTags).map(t -> tagRepository.findTagByQuery(t).get())
        .collect(Collectors.toSet());
    return setTags;
  }

  private Post getPost() {
    Post post = Post.builder()
        .user(getUser())
        .moderationStatus(ModerationStatus.NEW)
        .title("hello world again!")
        .isActive(1)
        .tags(getTags("java,php"))
        .moderator(getUser())
        .text(getText())
        .time(LocalDateTime.now())
        .view_count(0)
        .build();
    return post;
  }
}