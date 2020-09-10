package main.mapper;


import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import main.dto.response.PostByIdResponse;
import main.dto.response.ResponsePostApi;
import main.dto.response.ResponsePostApiToModeration;
import main.dto.response.ResponsePostApiWithAnnounce;
import main.model.Post;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class PostMapper {

  @Autowired
  UserMapper userMapper;

  public ResponsePostApi postToResponsePostApi(Post post) {
    ResponsePostApi responsePostApi = new ResponsePostApi();
    responsePostApi.setId(post.getId());
    LocalDateTime time = post.getTime().minusHours(3);
    ZonedDateTime timeZoned = time.atZone(ZoneId.systemDefault());
    ZonedDateTime utcZoned = timeZoned.withZoneSameInstant(ZoneId.of("UTC"));
    responsePostApi.setTimestamp(utcZoned.toInstant().getEpochSecond());
    responsePostApi.setUser(userMapper.userToUserApi(post.getUser()));
    responsePostApi.setTitle(post.getTitle());
    responsePostApi.setText(post.getText());
    responsePostApi.setViewCount(post.getViewCount());
    responsePostApi.setCommentCount(post.getPostComments().size());
    int likes = post.getPostVotes().stream().filter(p -> p.getValue() == 1)
        .collect(Collectors.toSet()).size();
    int dislikes = post.getPostVotes().stream().filter(p -> p.getValue() == -1)
        .collect(Collectors.toSet()).size();
    responsePostApi.setLikeCount(likes);
    responsePostApi.setDislikeCount(dislikes);
    return responsePostApi;
  }

  public List<ResponsePostApi> postToResponsePostApi(List<Post> posts) {
    List<ResponsePostApi> postApiList = new ArrayList<>();
    for (Post post : posts) {
      postApiList.add(postToResponsePostApi(post));
    }
    return postApiList;
  }

  public PostByIdResponse postToPostById(Post post) {
    PostByIdResponse postByIdApi = new PostByIdResponse();
    postByIdApi.setId(post.getId());
    LocalDateTime time = post.getTime().minusHours(3);
    ZonedDateTime timeZoned = time.atZone(ZoneId.systemDefault());
    ZonedDateTime utcZoned = timeZoned.withZoneSameInstant(ZoneId.of("UTC"));
    postByIdApi.setTimestamp(utcZoned.toInstant().getEpochSecond());
    postByIdApi.setUser(userMapper.userToUserApi(post.getUser()));
    postByIdApi.setTitle(post.getTitle());
    postByIdApi.setText(post.getText());
    postByIdApi.setViewCount(post.getViewCount());
    postByIdApi.setCommentCount(post.getPostComments().size());
    return postByIdApi;
  }

  public ResponsePostApiToModeration postToResponsePostApiToModeration(Post post) {
    ResponsePostApiToModeration postApiToModeration = new ResponsePostApiToModeration();
    postApiToModeration.setId(post.getId());
    postApiToModeration.setTime(post.getTime());
    postApiToModeration.setUser(userMapper.userToUserApi(post.getUser()));
    postApiToModeration.setTitle(post.getTitle());
    postApiToModeration.setAnnounce(post.getText());
    return postApiToModeration;
  }

  public ResponsePostApiWithAnnounce responsePostApiToResponseWithAnnounce
      (ResponsePostApi responsePostApi) {
    ResponsePostApiWithAnnounce response = new ResponsePostApiWithAnnounce();
    response.setId(responsePostApi.getId());
    response.setTimestamp(responsePostApi.getTimestamp());
    response.setUser(responsePostApi.getUser());
    response.setTitle(responsePostApi.getTitle());
    String text = responsePostApi.getText();
    String announce = text.replaceAll("\\<.*?\\>", "");
    if (announce.length() > 200) {
      response.setAnnounce(announce.substring(0, 200));
    } else {
      response.setAnnounce(announce);
    }
    response.setLikeCount(responsePostApi.getLikeCount());
    response.setDislikeCount(responsePostApi.getDislikeCount());
    response.setCommentCount(responsePostApi.getCommentCount());
    response.setViewCount(responsePostApi.getViewCount());
    return response;
  }
}
