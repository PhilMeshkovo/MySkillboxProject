package main.mapper;


import java.util.ArrayList;
import java.util.List;
import main.api.response.ResponsePostApi;
import main.model.Post;
import org.mapstruct.Mapper;

@Mapper
public class PostMapper {

  UserMapper userMapper = new UserMapper();

  public ResponsePostApi postToResponsePostApi(Post post) {
    ResponsePostApi responsePostApi = new ResponsePostApi();
    responsePostApi.setId(post.getId());
    responsePostApi.setTime(post.getTime());
    responsePostApi.setUser(userMapper.userToUserApi(post.getUser()));
    responsePostApi.setTitle(post.getTitle());
    responsePostApi.setAnnounce(post.getText());
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

  ;

}
