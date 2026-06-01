package main.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.text.ParseException;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import jakarta.persistence.EntityNotFoundException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import main.TestFixtures;
import main.dto.request.AddPostRequest;
import main.dto.request.PostCommentRequest;
import main.dto.request.PostLikeRequest;
import main.dto.request.PostModerationRequest;
import main.dto.response.PostListResponse;
import main.dto.response.ResultResponseWithErrors;
import main.mapper.CommentMapper;
import main.mapper.PostMapper;
import main.model.GlobalSettings;
import main.model.Post;
import main.model.PostComment;
import main.model.PostVotes;
import main.model.Tag;
import main.model.User;
import main.model.enums.ModerationStatus;
import main.repository.GlobalSettingsRepository;
import main.repository.PostCommentRepository;
import main.repository.PostRepository;
import main.repository.PostVotesRepository;
import main.repository.TagRepository;
import main.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class PostServiceUnitTest {

  @Mock PostRepository postRepository;
  @Mock UserRepository userRepository;
  @Mock HttpServletRequest request;
  @Mock HttpSession session;
  @Mock PostCommentRepository postCommentRepository;
  @Mock PostVotesRepository postVotesRepository;
  @Mock TagRepository tagRepository;
  @Mock PostMapper postMapper;
  @Mock CommentMapper commentMapper;
  @Mock GlobalSettingsRepository globalSettingsRepository;
  @Mock AuthenticationService authenticationService;

  PostService service;
  User user;
  Post post;
  Map<String, Integer> authorizedUsers;

  @BeforeEach
  void setUp() {
    service = new PostService(postRepository, userRepository, request, postCommentRepository,
        postVotesRepository, tagRepository, postMapper, commentMapper, globalSettingsRepository,
        authenticationService);
    ReflectionTestUtils.setField(service, "textMin", 10);
    ReflectionTestUtils.setField(service, "titleMin", 3);
    user = TestFixtures.user(5, 1);
    post = TestFixtures.post(9, user, ModerationStatus.ACCEPTED);
    authorizedUsers = new HashMap<>();
    authorizedUsers.put("session", user.getId());
    lenient().when(request.getSession()).thenReturn(session);
    lenient().when(session.getId()).thenReturn("session");
    lenient().when(authenticationService.getAuthorizedUsers()).thenReturn(authorizedUsers);
  }

  @Test
  void getAllPostsSupportsAllSortModesAndMapsResponses() {
    when(postMapper.postToResponsePostApi(post)).thenReturn(TestFixtures.responsePostApi(9));
    when(postMapper.responsePostApiToResponseWithAnnounce(any())).thenReturn(TestFixtures.responseWithAnnounce(9));
    when(postRepository.findAllPostsOrderedByTimeDesc(0, 10)).thenReturn(List.of(post));
    when(postRepository.findAllPostsOrderedByTime(0, 10)).thenReturn(List.of(post));
    when(postRepository.findAllPostsSortedByComments(0, 10)).thenReturn(List.of(post));
    when(postRepository.findAllPostsSortedByLikes(0, 10)).thenReturn(List.of(post));

    assertEquals(1, service.getAllPosts(0, 10, "RECENT").getCount());
    assertEquals(1, service.getAllPosts(0, 10, "EARLY").getCount());
    assertEquals(1, service.getAllPosts(0, 10, "POPULAR").getCount());
    assertEquals(1, service.getAllPosts(0, 10, "BEST").getCount());
  }

  @Test
  void queryTagDateMyAndModerationSearchesReturnLists() throws ParseException {
    when(postMapper.postToResponsePostApi(post)).thenReturn(TestFixtures.responsePostApi(9));
    when(postMapper.responsePostApiToResponseWithAnnounce(any())).thenReturn(TestFixtures.responseWithAnnounce(9));
    Tag tag = post.getTags().iterator().next();
    when(tagRepository.findTagByQuery("tag9")).thenReturn(Optional.of(tag));
    when(postRepository.findByIdIn(anyList(), eq(0), eq(10))).thenReturn(List.of(post));
    when(postRepository.findPostByQuery(0, 10, "spring")).thenReturn(List.of(post));
    when(postRepository.findAllPostsByTime(eq(0), eq(10), any())).thenReturn(List.of(post));
    when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));
    when(postRepository.findAllMyPostsInactive(0, 10, user.getId())).thenReturn(List.of(post));
    when(postRepository.findAllMyPosts(0, 10, "NEW", user.getId())).thenReturn(List.of(post));
    when(postRepository.findAllMyPosts(0, 10, "DECLINED", user.getId())).thenReturn(List.of(post));
    when(postRepository.findAllMyPosts(0, 10, "ACCEPTED", user.getId())).thenReturn(List.of(post));
    when(postRepository.findAllPostsToModeration(0, 10, "new")).thenReturn(List.of(post));

    assertEquals(1, service.getAllPostsByTextAndTitle(0, 10, "spring").getCount());
    assertEquals(1, service.getAllPostsByTag(0, 10, "tag9").getCount());
    assertEquals(1, service.getAllPostsByDate(0, 10, "2020-01-02").getCount());
    assertThrows(EntityNotFoundException.class, () -> service.getAllPostsByTag(0, 10, "missing"));
    when(postRepository.findAllPostsByTime(eq(1), eq(1), any())).thenReturn(List.of());
    assertThrows(EntityNotFoundException.class, () -> service.getAllPostsByDate(1, 1, "2020-01-02"));

    assertEquals(1, service.getAllMyPosts(0, 10, "inactive").getCount());
    assertEquals(1, service.getAllMyPosts(0, 10, "pending").getCount());
    assertEquals(1, service.getAllMyPosts(0, 10, "declined").getCount());
    assertEquals(1, service.getAllMyPosts(0, 10, "published").getCount());
    assertEquals(1, service.getAllPostsToModeration(0, 10, "new").getCount());
  }

  @Test
  void findPostByIdMapsVisiblePostAndIncrementsView() {
    authorizedUsers.clear();
    when(postRepository.findById(post.getId())).thenReturn(Optional.of(post));
    when(postRepository.getOne(post.getId())).thenReturn(post);
    when(postMapper.postToPostById(post)).thenReturn(new main.dto.response.PostByIdResponse());
    when(postCommentRepository.findCommentsByPostId(post.getId())).thenReturn(List.copyOf(post.getPostComments()));
    when(commentMapper.postCommentListToCommentApi(anyList())).thenReturn(List.of());

    assertNotNull(service.findPostById(post.getId()));
    assertEquals(8, post.getViewCount());
  }

  @Test
  void addAndUpdatePostValidateAndPersist() {
    GlobalSettings settings = new GlobalSettings();
    settings.setValue("NO");
    when(globalSettingsRepository.findByCode("POST_PREMODERATION")).thenReturn(Optional.of(settings));
    when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));
    Tag java = new Tag();
    java.setName("java");
    when(tagRepository.findAll()).thenReturn(List.of());
    when(tagRepository.findTagByQuery("java")).thenReturn(Optional.of(java));

    AddPostRequest request = validPostRequest();
    ResultResponseWithErrors addResponse = service.addPost(request);
    assertTrue(addResponse.isResult());
    verify(postRepository).save(argThat(saved -> saved.getModerationStatus() == ModerationStatus.ACCEPTED));

    ResultResponseWithErrors invalid = service.addPost(shortPostRequest());
    assertFalse(invalid.isResult());
    assertEquals("Заголовок не установлен или слишком кроткий", invalid.getErrors().getTitle());

    when(postRepository.findById(post.getId())).thenReturn(Optional.of(post));
    when(postRepository.getOne(post.getId())).thenReturn(post);
    when(tagRepository.findTagByQuery("java")).thenReturn(Optional.of(java));
    assertTrue(service.updatePost(post.getId(), request).isResult());
    assertFalse(service.updatePost(post.getId(), shortPostRequest()).isResult());

    User another = TestFixtures.user(99, 0);
    post.setUser(another);
    assertThrows(EntityNotFoundException.class, () -> service.updatePost(post.getId(), request));
  }

  @Test
  void addCommentReturnsOkOrBadRequestWithErrors() {
    when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));
    when(postRepository.findById(post.getId())).thenReturn(Optional.of(post));
    PostComment saved = new PostComment();
    saved.setId(22);
    when(postCommentRepository.save(any())).thenReturn(saved);

    ResponseEntity ok = service.addCommentToPost(new PostCommentRequest(post.getId(), "long enough text"));
    assertEquals(HttpStatus.OK, ok.getStatusCode());

    ResponseEntity bad = service.addCommentToPost(new PostCommentRequest(123, "short"));
    assertEquals(HttpStatus.BAD_REQUEST, bad.getStatusCode());
  }

  @Test
  void tagsModerationCalendarAndVotesAreCovered() {
    Tag tag = post.getTags().iterator().next();
    when(postRepository.findAll()).thenReturn(List.of(post));
    when(tagRepository.findAll()).thenReturn(List.of(tag));
    when(tagRepository.findTagByQuery("tag9")).thenReturn(Optional.of(tag));
    assertEquals(1, service.getTag("").getTags().size());
    assertEquals(1, service.getTag("tag9").getTags().size());

    when(postRepository.findById(post.getId())).thenReturn(Optional.of(post));
    when(postRepository.getOne(post.getId())).thenReturn(post);
    when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));
    PostModerationRequest moderation = new PostModerationRequest();
    moderation.setPostId(post.getId());
    moderation.setDecision("decline");
    assertTrue(service.moderationPost(moderation).isResult());
    assertEquals(ModerationStatus.DECLINED, post.getModerationStatus());
    moderation.setDecision("accept");
    assertTrue(service.moderationPost(moderation).isResult());
    assertEquals(ModerationStatus.ACCEPTED, post.getModerationStatus());

    assertEquals(2020, service.getAllPostsInYear("2020").get("years").get(0).asInt());
    assertTrue(service.getAllPostsInYear("").has("posts"));

    PostLikeRequest voteRequest = new PostLikeRequest();
    voteRequest.setPostId(post.getId());
    when(postVotesRepository.findByPostIdAndUserId(post.getId(), user.getId())).thenReturn(Optional.empty());
    assertTrue(service.postLike(voteRequest).isResult());
    assertTrue(service.postDislike(voteRequest).isResult());

    PostVotes existingDislike = PostVotes.builder().id(1).value(-1).build();
    when(postVotesRepository.findByPostIdAndUserId(post.getId(), user.getId())).thenReturn(Optional.of(existingDislike));
    when(postVotesRepository.getOne(1)).thenReturn(existingDislike);
    assertTrue(service.postLike(voteRequest).isResult());
    assertEquals(1, existingDislike.getValue());

    PostVotes existingLike = PostVotes.builder().id(2).value(1).build();
    when(postVotesRepository.findByPostIdAndUserId(post.getId(), user.getId())).thenReturn(Optional.of(existingLike));
    when(postVotesRepository.getOne(2)).thenReturn(existingLike);
    assertTrue(service.postDislike(voteRequest).isResult());
    assertEquals(-1, existingLike.getValue());

    authorizedUsers.clear();
    assertThrows(EntityNotFoundException.class, () -> service.postLike(voteRequest));
    assertThrows(EntityNotFoundException.class, () -> service.postDislike(voteRequest));
  }

  private AddPostRequest validPostRequest() {
    AddPostRequest request = new AddPostRequest();
    request.setTimestamp(1_600_000_000L);
    request.setActive(1);
    request.setTitle("Valid title");
    request.setText("Valid long post text");
    request.setTags(new String[] {"java"});
    return request;
  }

  private AddPostRequest shortPostRequest() {
    AddPostRequest request = new AddPostRequest();
    request.setTimestamp(1L);
    request.setActive(1);
    request.setTitle("no");
    request.setText("short");
    return request;
  }
}
