package main.mapper;

import static org.junit.jupiter.api.Assertions.*;

import java.util.List;
import main.TestFixtures;
import main.dto.response.CommentsApiResponse;
import main.dto.response.PostByIdResponse;
import main.dto.response.ResponsePostApi;
import main.dto.response.ResponsePostApiWithAnnounce;
import main.dto.response.UserApiWithPhoto;
import main.dto.response.UserResponse;
import main.model.Post;
import main.model.User;
import main.model.enums.ModerationStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

class MapperTest {

  private final UserMapper userMapper = new UserMapper();
  private final PostMapper postMapper = new PostMapper();
  private final CommentMapper commentMapper = new CommentMapper();

  @BeforeEach
  void setUp() {
    ReflectionTestUtils.setField(postMapper, "userMapper", userMapper);
    ReflectionTestUtils.setField(commentMapper, "userMapper", userMapper);
  }

  @Test
  void userMapperMapsBasicAndPhotoResponses() {
    User user = TestFixtures.user(10, 0);

    UserResponse basic = userMapper.userToUserApi(user);
    UserApiWithPhoto withPhoto = userMapper.userToUserWithPhoto(user);

    assertEquals(10, basic.getId());
    assertEquals("User 10", basic.getName());
    assertEquals("/photo10.jpg", withPhoto.getPhoto());
  }

  @Test
  void postMapperMapsCountsTimeAndAnnounce() {
    Post post = TestFixtures.post(1, TestFixtures.user(1, 0), ModerationStatus.ACCEPTED);

    ResponsePostApi response = postMapper.postToResponsePostApi(post);
    PostByIdResponse byId = postMapper.postToPostById(post);
    ResponsePostApiWithAnnounce announce = postMapper.responsePostApiToResponseWithAnnounce(response);

    assertEquals(1, response.getLikeCount());
    assertEquals(1, response.getDislikeCount());
    assertEquals(1, response.getCommentCount());
    assertTrue(byId.isActive());
    assertFalse(announce.getAnnounce().contains("<b>"));
    assertEquals(200, announce.getAnnounce().length());
    assertEquals(1, postMapper.postToResponsePostApi(List.of(post)).size());
  }

  @Test
  void commentMapperMapsList() {
    Post post = TestFixtures.post(2, TestFixtures.user(2, 0), ModerationStatus.ACCEPTED);

    List<CommentsApiResponse> comments = commentMapper.postCommentListToCommentApi(
        List.copyOf(post.getPostComments()));

    assertEquals(1, comments.size());
    assertEquals("Useful comment text", comments.get(0).getText());
    assertEquals("/photo2.jpg", ((UserApiWithPhoto) comments.get(0).getUser()).getPhoto());
  }
}
