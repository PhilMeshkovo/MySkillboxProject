package main.controller;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import java.io.IOException;
import jakarta.persistence.EntityNotFoundException;
import main.dto.request.*;
import main.dto.response.PostListResponse;
import main.dto.response.ResponseApiInit;
import main.dto.response.ResultResponse;
import main.dto.response.ResultResponseWithErrors;
import main.exception.UnauthorizedException;
import main.service.InitService;
import main.service.PostService;
import main.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class ControllerUnitTest {

  @Mock UserService userService;
  @Mock PostService postService;
  @Mock InitService initService;

  ApiAuthController authController;
  ApiPostController postController;
  ApiGeneralController generalController;

  @BeforeEach
  void setUp() {
    authController = new ApiAuthController();
    postController = new ApiPostController();
    generalController = new ApiGeneralController();
    ReflectionTestUtils.setField(authController, "userService", userService);
    ReflectionTestUtils.setField(postController, "postService", postService);
    ReflectionTestUtils.setField(generalController, "initService", initService);
    ReflectionTestUtils.setField(generalController, "userService", userService);
    ReflectionTestUtils.setField(generalController, "mapper", new ObjectMapper());
  }

  @Test
  void authControllerDelegatesAndHandlesProfileErrors() throws Exception {
    ResultResponse result = new ResultResponse();
    result.resultSuccess();
    ResultResponseWithErrors withErrors = new ResultResponseWithErrors();
    withErrors.resultSuccess();
    ObjectNode email = new ObjectMapper().createObjectNode().put("email", "mail@test");
    when(userService.saveUser(any())).thenReturn(withErrors);
    when(userService.login(any())).thenReturn(new main.dto.response.ResultResponseWithUserDto());
    when(userService.check()).thenReturn(new main.dto.response.ResultResponseWithUserDto());
    when(userService.restore("mail@test")).thenReturn(result);
    when(userService.postNewPassword(any())).thenReturn(withErrors);
    when(userService.postNewProfileWithPhoto(any())).thenReturn(withErrors);
    when(userService.postNewProfile(any())).thenReturn(withErrors);
    when(userService.getMyStatistics()).thenReturn(email);
    when(userService.getAllStatistics()).thenReturn(email);
    when(userService.logout()).thenReturn(result);
    when(userService.getCaptcha()).thenReturn(email);

    assertEquals(HttpStatus.OK, authController.addUser(new RegisterFormRequest()).getStatusCode());
    assertEquals(HttpStatus.OK, authController.login(new LoginRequest()).getStatusCode());
    assertEquals(HttpStatus.OK, authController.checkUser().getStatusCode());
    assertEquals(HttpStatus.OK, authController.restore(email).getStatusCode());
    assertEquals(HttpStatus.OK, authController.postNewPassword(new ChangePasswordRequest("c", "p", "c", "s")).getStatusCode());
    assertEquals(HttpStatus.OK, authController.postNewProfileWithPhoto(new PostProfileRequestWithPhoto()).getStatusCode());
    assertEquals(HttpStatus.OK, authController.postNewProfile(new PostProfileRequest()).getStatusCode());
    assertEquals(HttpStatus.OK, authController.getMyStatistics().getStatusCode());
    assertEquals(HttpStatus.OK, authController.getAllStatistics().getStatusCode());
    assertEquals(HttpStatus.OK, authController.logout().getStatusCode());
    assertEquals(HttpStatus.OK, authController.getCaptcha().getStatusCode());

    when(userService.postNewProfileWithPhoto(any())).thenThrow(new RuntimeException("bad"));
    assertEquals(HttpStatus.BAD_REQUEST, authController.postNewProfileWithPhoto(new PostProfileRequestWithPhoto()).getStatusCode());
    assertEquals("missing", authController.handleNotFoundException(new javassist.NotFoundException("missing")));
  }

  @Test
  void postControllerDelegatesSuccessAndErrorBranches() throws Exception {
    PostListResponse list = new PostListResponse(java.util.List.of(), 0);
    when(postService.getAllPosts(anyInt(), anyInt(), anyString())).thenReturn(list);
    when(postService.getAllPostsByTextAndTitle(anyInt(), anyInt(), anyString())).thenReturn(list);
    when(postService.findPostById(1)).thenReturn(new main.dto.response.PostByIdResponse());
    when(postService.getAllPostsByDate(anyInt(), anyInt(), anyString())).thenReturn(list);
    when(postService.getAllPostsByTag(anyInt(), anyInt(), anyString())).thenReturn(list);
    when(postService.getAllMyPosts(anyInt(), anyInt(), anyString())).thenReturn(list);
    when(postService.getAllPostsToModeration(anyInt(), anyInt(), anyString())).thenReturn(list);
    when(postService.addPost(any())).thenReturn(new ResultResponseWithErrors());
    when(postService.updatePost(anyInt(), any())).thenReturn(new ResultResponseWithErrors());
    when(postService.addCommentToPost(any())).thenReturn(new ResponseEntity<>(HttpStatus.OK));
    when(postService.getTag(anyString())).thenReturn(new main.dto.response.ListTagsResponse());
    ResultResponse result = new ResultResponse();
    result.resultSuccess();
    when(postService.moderationPost(any())).thenReturn(result);
    when(postService.getAllPostsInYear(anyString())).thenReturn(new ObjectMapper().createObjectNode());
    when(postService.postLike(any())).thenReturn(result);
    when(postService.postDislike(any())).thenReturn(result);

    assertSame(list, postController.getAllPosts(0, 10, "recent"));
    assertEquals(HttpStatus.OK, postController.getAllPostsByTagAndTitle(0, 10, "q").getStatusCode());
    assertEquals(HttpStatus.OK, postController.getPostById(1).getStatusCode());
    assertEquals(HttpStatus.OK, postController.getAllPostsByDate(0, 10, "2020-01-01").getStatusCode());
    assertEquals(HttpStatus.OK, postController.getAllPostsByTag(0, 10, "java").getStatusCode());
    assertSame(list, postController.getAllMyPosts(0, 10, "pending"));
    assertSame(list, postController.getAllPostsToModeration(0, 10, "new"));
    assertNotNull(postController.addPost(new AddPostRequest()));
    assertEquals(HttpStatus.OK, postController.updatePost(1, new AddPostRequest()).getStatusCode());
    assertEquals(HttpStatus.OK, postController.addComment(new PostCommentRequest()).getStatusCode());
    assertEquals(HttpStatus.OK, postController.getTags("").getStatusCode());
    assertEquals(HttpStatus.OK, postController.moderationPost(new PostModerationRequest()).getStatusCode());
    assertEquals(HttpStatus.OK, postController.getAllPostsInYear("2020").getStatusCode());
    assertEquals(HttpStatus.OK, postController.postLike(new PostLikeRequest()).getStatusCode());
    assertEquals(HttpStatus.OK, postController.postDislike(new PostLikeRequest()).getStatusCode());

    when(postService.getAllPostsByDate(anyInt(), anyInt(), eq("bad"))).thenThrow(new EntityNotFoundException("not found"));
    assertEquals(HttpStatus.NOT_FOUND, postController.getAllPostsByDate(0, 10, "bad").getStatusCode());
    when(postService.updatePost(eq(2), any())).thenThrow(new EntityNotFoundException("not found"));
    assertEquals(HttpStatus.NOT_FOUND, postController.updatePost(2, new AddPostRequest()).getStatusCode());
    when(postService.updatePost(eq(3), any())).thenThrow(new IllegalArgumentException("bad"));
    assertEquals(HttpStatus.BAD_REQUEST, postController.updatePost(3, new AddPostRequest()).getStatusCode());
    when(postService.getTag("bad")).thenThrow(new EntityNotFoundException("bad"));
    assertEquals(HttpStatus.BAD_REQUEST, postController.getTags("bad").getStatusCode());
    when(postService.moderationPost(any())).thenThrow(new RuntimeException("bad"));
    assertEquals(HttpStatus.BAD_REQUEST, postController.moderationPost(new PostModerationRequest()).getStatusCode());
    when(postService.postLike(any())).thenThrow(new EntityNotFoundException("unauthorized"));
    assertEquals(HttpStatus.UNAUTHORIZED, postController.postLike(new PostLikeRequest()).getStatusCode());
    when(postService.postDislike(any())).thenThrow(new EntityNotFoundException("unauthorized"));
    assertEquals(HttpStatus.UNAUTHORIZED, postController.postDislike(new PostLikeRequest()).getStatusCode());
    assertEquals("missing", postController.handleNotFoundException(new javassist.NotFoundException("missing")));
  }

  @Test
  void generalAndDefaultControllersDelegate() throws Exception {
    ResponseApiInit init = new ResponseApiInit();
    when(initService.getInit()).thenReturn(init);
    when(initService.uploadImage(any())).thenReturn("/api/image/1.jpg");
    when(initService.getImage("1.jpg")).thenReturn(new byte[] {1, 2});
    when(userService.getSettings()).thenReturn(new ObjectMapper().createObjectNode());

    assertSame(init, generalController.getInit());
    assertEquals(HttpStatus.OK, generalController.upload(new MockMultipartFile("image", new byte[] {1})).getStatusCode());
    assertArrayEquals(new byte[] {1, 2}, generalController.getImage("1.jpg"));
    assertEquals(HttpStatus.OK, generalController.getSettings().getStatusCode());
    generalController.putSettings(new GlobalSettingsRequest());
    verify(userService).putSettings(any());

    when(initService.uploadImage(any())).thenThrow(new IOException("bad"));
    assertEquals(HttpStatus.BAD_REQUEST, generalController.upload(new MockMultipartFile("image", new byte[] {1})).getStatusCode());
    when(initService.getImage("bad")).thenThrow(new IOException("bad"));
    assertEquals(0, generalController.getImage("bad").length);

    DefaultController defaultController = new DefaultController();
    assertEquals("index", defaultController.index());
    assertEquals("forward:/", defaultController.redirectToIndex());
  }
}
