package main;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;
import main.dto.response.ResponsePostApi;
import main.dto.response.ResponsePostApiWithAnnounce;
import main.dto.response.UserResponse;
import main.model.Post;
import main.model.PostComment;
import main.model.PostVotes;
import main.model.Role;
import main.model.Tag;
import main.model.User;
import main.model.enums.ModerationStatus;

public final class TestFixtures {

  public static final LocalDateTime POST_TIME = LocalDateTime.of(2020, 1, 2, 15, 30);

  private TestFixtures() {
  }

  public static User user(int id, int moderator) {
    return new User(id, moderator, LocalDateTime.of(2020, 1, 1, 12, 0), "User " + id,
        "user" + id + "@mail.test", "password", "code" + id, "/photo" + id + ".jpg",
        new Role(1, moderator == 1 ? "ROLE_MODERATOR" : "ROLE_USER"));
  }

  public static Post post(int id, User user, ModerationStatus status) {
    Post post = new Post();
    post.setId(id);
    post.setIsActive(1);
    post.setModerationStatus(status);
    post.setUser(user);
    post.setModerator(user);
    post.setTime(POST_TIME);
    post.setTitle("Title " + id);
    post.setText("<b>Long post text for tests</b> ".repeat(20));
    post.setViewCount(7);
    Tag tag = new Tag();
    tag.setId(id);
    tag.setName("tag" + id);
    tag.setPosts(Set.of(post));
    post.setTags(new HashSet<>(Set.of(tag)));

    PostComment comment = new PostComment();
    comment.setId(id);
    comment.setPost(post);
    comment.setUser(user);
    comment.setTime(POST_TIME.plusMinutes(1));
    comment.setText("Useful comment text");
    post.setPostComments(new HashSet<>(Set.of(comment)));

    PostVotes like = PostVotes.builder().id(id).post(post).user(user).time(POST_TIME).value(1).build();
    PostVotes dislike = PostVotes.builder().id(id + 100).post(post).user(user).time(POST_TIME).value(-1).build();
    post.setPostVotes(new HashSet<>(Set.of(like, dislike)));
    return post;
  }

  public static ResponsePostApi responsePostApi(int id) {
    UserResponse user = new UserResponse();
    user.setId(id);
    user.setName("User " + id);
    ResponsePostApi response = new ResponsePostApi();
    response.setId(id);
    response.setTimestamp(100L);
    response.setUser(user);
    response.setTitle("Title " + id);
    response.setText("<p>" + "announce ".repeat(40) + "</p>");
    response.setLikeCount(2);
    response.setDislikeCount(1);
    response.setCommentCount(3);
    response.setViewCount(4);
    return response;
  }

  public static ResponsePostApiWithAnnounce responseWithAnnounce(int id) {
    ResponsePostApiWithAnnounce response = new ResponsePostApiWithAnnounce();
    response.setId(id);
    response.setTimestamp(100L);
    response.setUser(responsePostApi(id).getUser());
    response.setTitle("Title " + id);
    response.setAnnounce("announce");
    response.setLikeCount(2);
    response.setDislikeCount(1);
    response.setCommentCount(3);
    response.setViewCount(4);
    return response;
  }
}
