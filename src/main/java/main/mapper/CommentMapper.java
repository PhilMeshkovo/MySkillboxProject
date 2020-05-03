package main.mapper;

import java.util.ArrayList;
import java.util.List;
import main.api.response.CommentsApi;
import main.model.PostComment;

public class CommentMapper {

  UserMapper userMapper = new UserMapper();

  public CommentsApi postCommentToCommentsApi(PostComment postComment) {
    CommentsApi commentsApi = new CommentsApi();
    commentsApi.setId(postComment.getId());
    commentsApi.setTime(postComment.getTime());
    commentsApi.setText(postComment.getText());
    commentsApi.setUser(userMapper.userToUserWithPhoto(postComment.getUser()));
    return commentsApi;
  }

  public List<CommentsApi> postCommentListToCommentApi(List<PostComment> postCommentList) {
    List<CommentsApi> commentsApiList = new ArrayList<>();
    for (PostComment postComment : postCommentList) {
      commentsApiList.add(postCommentToCommentsApi(postComment));
    }
    return commentsApiList;
  }
}
