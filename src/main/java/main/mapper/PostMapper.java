package main.mapper;


import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import main.dto.PostByIdApi;
import main.dto.ResponsePostApi;
import main.dto.ResponsePostApiToModeration;
import main.dto.ResponsePostApiWithAnnounce;
import main.model.Post;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class PostMapper {

  DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy hh-mm");

  @Autowired
  UserMapper userMapper;

  public ResponsePostApi postToResponsePostApi(Post post) {
    ResponsePostApi responsePostApi = new ResponsePostApi();
    responsePostApi.setId(post.getId());
    LocalDateTime time = post.getTime();
    ZonedDateTime timeZoned = time.atZone(ZoneId.systemDefault());
    ZonedDateTime utcZoned = timeZoned.withZoneSameInstant(ZoneId.of("UTC"));
    responsePostApi.setTimestamp(utcZoned.toInstant().getEpochSecond());
    responsePostApi.setUser(userMapper.userToUserApi(post.getUser()));
    responsePostApi.setTitle(post.getTitle());
    responsePostApi.setText(post.getText());
    responsePostApi.setViewCount(post.getView_count());
    return responsePostApi;
  }

  public ResponsePostApi postToResponsePostApiWithEmailName(Post post) {
    ResponsePostApi responsePostApi = new ResponsePostApi();
    responsePostApi.setId(post.getId());
    LocalDateTime time = post.getTime();
    ZonedDateTime timeZoned = time.atZone(ZoneId.systemDefault());
    ZonedDateTime utcZoned = timeZoned.withZoneSameInstant(ZoneId.of("UTC"));
    responsePostApi.setTimestamp(utcZoned.toInstant().getEpochSecond());
    responsePostApi.setUser(userMapper.userToUserApiWithEmailName(post.getUser()));
    responsePostApi.setTitle(post.getTitle());
    responsePostApi.setText(post.getText());
    responsePostApi.setViewCount(post.getView_count());
    return responsePostApi;
  }

  public List<ResponsePostApi> postToResponsePostApi(List<Post> posts) {
    List<ResponsePostApi> postApiList = new ArrayList<>();
    for (Post post : posts) {
      postApiList.add(postToResponsePostApi(post));
    }
    return postApiList;
  }

  public PostByIdApi postToPostById(Post post) {
    PostByIdApi postByIdApi = new PostByIdApi();
    postByIdApi.setId(post.getId());
    LocalDateTime time = post.getTime();
    ZonedDateTime timeZoned = time.atZone(ZoneId.systemDefault());
    ZonedDateTime utcZoned = timeZoned.withZoneSameInstant(ZoneId.of("UTC"));
    postByIdApi.setTimestamp(utcZoned.toInstant().getEpochSecond());
    postByIdApi.setUser(userMapper.userToUserApi(post.getUser()));
    postByIdApi.setTitle(post.getTitle());
    postByIdApi.setText(post.getText());
    postByIdApi.setViewCount(post.getView_count());

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
