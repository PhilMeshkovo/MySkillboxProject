package main.service;

import com.fasterxml.jackson.databind.JsonNode;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import main.dto.AddPostDto;
import main.dto.PostByIdApi;
import main.dto.PostListApi;
import main.model.GlobalSettings;
import main.model.Post;
import main.model.Tag;
import main.model.User;
import main.model.enums.ModerationStatus;
import main.repository.GlobalSettingsRepository;
import main.repository.PostRepository;
import main.repository.TagRepository;
import main.repository.UserRepository;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
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
  PostService postService;

  @MockBean
  PostRepository postRepository;

  @MockBean
  TagRepository tagRepository;

  @MockBean
  UserRepository userRepository;

  @Mock
  private HttpServletRequest request;

  @MockBean
  GlobalSettingsRepository globalSettingsRepository;

  @MockBean
  HttpSession httpSession;

  @MockBean
  AuthenticationService authenticationService;

  private final Map<String, Integer> authorizedUsers = new HashMap<>();

  @Bean
  public PasswordEncoder passwordEncoder() {
    return new BCryptPasswordEncoder();
  }

  void initAuthorizedUsers() {
    authorizedUsers.put("1", 0);
    authorizedUsers.put("2", 0);
    authorizedUsers.put("3", 0);
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
    Mockito.doReturn(httpSession).when(request).getSession();
    initAuthorizedUsers();
    Mockito.doReturn(authorizedUsers).when(authenticationService).getAuthorizedUsers();
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
    Post post = getPost();
    AddPostDto addPostDto = new AddPostDto();
    addPostDto.setTitle(post.getTitle());
    addPostDto.setTimestamp(0L);
    addPostDto.setText(getText());
    addPostDto.setActive(1);
    GlobalSettings globalSettings = new GlobalSettings();
    globalSettings.setValue("YES");
    Mockito.doReturn(Optional.of(globalSettings)).when(globalSettingsRepository).findById(2);
    Mockito.doReturn("1").when(httpSession).getId();
    Mockito.doReturn(httpSession).when(request).getSession();
    initAuthorizedUsers();
    Mockito.doReturn(authorizedUsers).when(authenticationService).getAuthorizedUsers();
    Mockito.doReturn(Optional.of(getUser())).when(userRepository).findById(0);
    Mockito.doReturn(post).when(postRepository).save(post);
    JsonNode jsonNode = postService.addPost(addPostDto);
    Assertions.assertTrue(jsonNode.get("result").asBoolean());
  }

  @Test
  void updatePost() {
    User user = getUser();
    Post post = getPost();
    AddPostDto addPostDto = new AddPostDto();
    addPostDto.setActive(1);
    addPostDto.setText(getText());
    addPostDto.setTitle("DDDDDDDDDDDDDDDDDDDDDD");
    addPostDto.setTimestamp(0L);
    Mockito.doReturn(httpSession)
        .when(request).getSession();
    initAuthorizedUsers();
    Mockito.doReturn(authorizedUsers).when(authenticationService).getAuthorizedUsers();
    Mockito.doReturn(Optional.of(user))
        .when(userRepository).findById(0);
    Mockito.doReturn(Optional.of(post)).when(postRepository).findById(0);
    Mockito.doReturn(post).when(postRepository).getOne(0);
    JsonNode jsonNode = postService.updatePost(0, addPostDto);
    Assertions.assertTrue(jsonNode.get("result").asBoolean());
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
    user.setId(0);
    user.setEmail("some@mail.ru");
    user.setName("vanya");
    user.setRegTime(LocalDateTime.of(2020, 5, 3, 2, 20));
    user.setPassword("123456");
    user.setCode("123456");
    return user;
  }

  private Post getPost() {
    Tag tag = new Tag();
    tag.setId(0);
    tag.setName(null);
    tag.setPosts(Set.of());
    return Post.builder()
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
  }
}