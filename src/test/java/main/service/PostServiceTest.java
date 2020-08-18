package main.service;

import com.fasterxml.jackson.databind.JsonNode;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import main.dto.AddPostDto;
import main.dto.PostByIdApi;
import main.dto.PostCommentDto;
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
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Bean;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.junit4.SpringRunner;


@RunWith(SpringRunner.class)
@SpringBootTest
class PostServiceTest {

  @Autowired
  private PostService postService;

  @MockBean
  private PostRepository postRepository;

  @MockBean
  private TagRepository tagRepository;

  @MockBean
  PostCommentRepository postCommentRepository;

  @MockBean
  AuthenticationService authenticationService;

  @Bean
  public PasswordEncoder passwordEncoder() {
    return new BCryptPasswordEncoder();
  }

  @Test
  void getAllPosts() {
    Post post = getPost();
    Mockito.doReturn(List.of(post)).when(postRepository).findAllPostsOrderedByTimeDesc(0, 10);
    PostListApi postListApi = postService.getAllPosts(0, 10, "RECENT");
    Assertions.assertEquals(1, postListApi.getCount());
  }

  @Test
  void getAllPostsByTextAndTitle() {
    Post post = getPost();
    Mockito.doReturn(List.of(post)).when(postRepository).findPostByQuery(0, 10, "on");
    PostListApi postListApi = postService.getAllPostsByTextAndTitle(0, 10, "on");
    Assertions.assertEquals(1, postListApi.getCount());
  }

  @Test
  void getAllPostsByTag() {
    Post post = getPost();
    Tag tag = new Tag();
    tag.setId(1);
    tag.setName("java");
    tag.setPosts(Set.of(post));
    Mockito.doReturn(Optional.of(tag)).when(tagRepository).findTagByQuery("java");
    Mockito.doReturn(List.of(post)).when(postRepository).findByIdIn(List.of(0), 0, 10);
    PostListApi postListApi = postService.getAllPostsByTag(0, 10, "java");
    Assertions.assertEquals(1, postListApi.getCount());
  }

  @Test
  void getAllPostsByDate() throws ParseException {
    Post post = getPost();
    SimpleDateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd");
    Date localDate = dateFormatter.parse("1971-01-01");
    Mockito.doReturn(List.of(post)).when(postRepository).findAllPostsByTime(0, 10, localDate);
    PostListApi postListApi = postService.getAllPostsByDate(0, 10, "1971-01-01");
    Assertions.assertEquals(1, postListApi.getCount());
  }

  @Test
  void findPostById() {
    Post post = getPost();
    Mockito.doReturn(Optional.of(post)).when(postRepository).findById(0);
    Mockito.doReturn(post).when(postRepository).getOne(0);
    PostByIdApi postByIdApi = postService.findPostById(0);
    Assertions.assertEquals(0, postByIdApi.getId());
    Assertions.assertEquals("hello world again!", postByIdApi.getTitle());
  }

  @Test
  void getAllPostsToModeration() {
    Mockito.doReturn(List.of(getPost())).when(postRepository)
        .findAllPostsToModeration(0, 10, "NEW");
    PostListApi postListApi = postService.getAllPostsToModeration(0, 10, "NEW");
    Assertions.assertEquals(1, postListApi.getCount());
  }

  @Test
  void addPost() {
    Post post = Post.builder()
        .id(0)
        .user(getUser())
        .moderationStatus(ModerationStatus.NEW)
        .title("hello world again!")
        .isActive(1)
        .tags(Set.of())
        .moderator(getUser())
        .text(getText())
        .time(LocalDateTime.now())
        .view_count(0)
        .build();
    Mockito.doReturn(post).when(postRepository).save(post);
    Assertions.assertEquals(0, post.getId());
  }

  @Test
  void updatePost() throws Exception {
    User user = getUser();
    Tag tag = new Tag();
    tag.setId(0);
    tag.setName("java");
    tag.setPosts(Set.of(getPost()));
    Post post = getPost();
    AddPostDto addPostDto = new AddPostDto();
    addPostDto.setActive(1);
    addPostDto.setText(getText());
    addPostDto.setTitle("DDDDDDDDDDDDDDDDDDDDDD");
    addPostDto.setTimestamp(0L);
    String[] tags = {"java"};
    addPostDto.setTags(tags);
    Mockito.doReturn(user)
        .when(authenticationService).getCurrentUser();
    Mockito.doReturn(Optional.of(post)).when(postRepository).findById(0);
    Mockito.doReturn(post).when(postRepository).getOne(0);
    Mockito.doReturn(Optional.of(tag)).when(tagRepository).findTagByQuery("java");
    JsonNode jsonNode = postService.updatePost(0, addPostDto);
    Assertions.assertTrue(jsonNode.get("result").asBoolean());
  }

  @Test
  void addCommentToPost() throws Exception {
    Mockito.doReturn(new User(1, 1, LocalDateTime.now(), "vanya",
        "some@mail.ru", "123456", "123456", "123.jpr", new Role(1)))
        .when(authenticationService).getCurrentUser();
    PostComment postComment = PostComment.builder()
        .id(0)
        .post(getPost())
        .parent(getPostComment())
        .text(getText())
        .time(LocalDateTime.now())
        .user(getUser())
        .build();
    PostCommentDto postCommentDto = new PostCommentDto();
    postCommentDto.setParent_id(0);
    postCommentDto.setPost_id(0);
    postCommentDto.setText("AAAAAAAAAAAAAAAAAA");
    Mockito.doReturn(postComment).when(postCommentRepository).save(postComment);
    Mockito.doReturn(Optional.of(getPost())).when(postRepository).findById(0);
    Mockito.doReturn(Optional.of(getPostComment())).when(postCommentRepository).findById(0);
    JsonNode jsonNode = postService.addCommentToPost(postCommentDto);
    Assertions.assertEquals("No parent comment on this post",
        jsonNode.get("error").get("parent").asText());
  }

  @Test
  void getAllPostsInYear() {
    Mockito.doReturn(List.of(getPost())).when(postRepository).findAll();
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
    user.setId(1);
    user.setEmail("some@mail.ru");
    user.setName("vanya");
    user.setRegTime(LocalDateTime.now());
    user.setPassword("123456");
    user.setCode("123456");
    return user;
  }

  private Post getPost() {
    Tag tag = new Tag();
    tag.setId(0);
    tag.setName("java");
    tag.setPosts(Set.of());
    Post post = Post.builder()
        .id(0)
        .user(getUser())
        .moderationStatus(ModerationStatus.ACCEPTED)
        .title("hello world again!")
        .isActive(1)
        .tags(Set.of(tag))
        .moderator(getUser())
        .text(getText())
        .time(LocalDateTime.now())
        .view_count(0)
        .build();
    return post;
  }

  private PostComment getPostComment() {
    PostComment postComment = PostComment.builder()
        .id(1)
        .user(getUser())
        .time(LocalDateTime.now())
        .text(getText())
        .post(getPost())
        .build();
    return postComment;
  }
}