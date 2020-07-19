package main.mapper;


import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import main.api.response.PostByIdApi;
import main.api.response.ResponsePostApi;
import main.api.response.ResponsePostApiToModeration;
import main.api.response.ResponsePostApiWithAnnounce;
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
    responsePostApi.setTime(post.getTime().format(formatter));
    responsePostApi.setUser(userMapper.userToUserApi(post.getUser()));
    responsePostApi.setTitle(post.getTitle());
    responsePostApi.setText(post.getText());
    responsePostApi.setViewCount(post.getView_count());
    return responsePostApi;
  }

  public ResponsePostApi postToResponsePostApiWithEmailName(Post post) {
    ResponsePostApi responsePostApi = new ResponsePostApi();
    responsePostApi.setId(post.getId());
    responsePostApi.setTime(post.getTime().format(formatter));
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
    postByIdApi.setTime(post.getTime().format(formatter));
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
      (ResponsePostApi responsePostApi){
    ResponsePostApiWithAnnounce response = new ResponsePostApiWithAnnounce();
    response.setId(responsePostApi.getId());
    response.setTime(responsePostApi.getTime());
    response.setUser(responsePostApi.getUser());
    response.setTitle(responsePostApi.getTitle());
    String text = responsePostApi.getText();
    String announce = text.replaceAll ("\\<.*?\\>", "");
    if (announce.length() > 200){
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
