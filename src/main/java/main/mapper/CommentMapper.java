package main.mapper;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import main.dto.response.CommentsApiResponse;
import main.model.PostComment;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class CommentMapper {

  @Autowired
  UserMapper userMapper;

  public CommentsApiResponse postCommentToCommentsApi(PostComment postComment) {
    CommentsApiResponse commentsApi = new CommentsApiResponse();
    commentsApi.setId(postComment.getId());
    LocalDateTime time = postComment.getTime();
    ZonedDateTime timeZoned = time.atZone(ZoneId.systemDefault());
    ZonedDateTime utcZoned = timeZoned.withZoneSameInstant(ZoneId.of("UTC"));
    commentsApi.setTimestamp(utcZoned.toInstant().getEpochSecond());
    commentsApi.setText(postComment.getText());
    commentsApi.setUser(userMapper.userToUserWithPhoto(postComment.getUser()));
    return commentsApi;
  }

  public List<CommentsApiResponse> postCommentListToCommentApi(List<PostComment> postCommentList) {
    List<CommentsApiResponse> commentsApiList = new ArrayList<>();
    for (PostComment postComment : postCommentList) {
      commentsApiList.add(postCommentToCommentsApi(postComment));
    }
    return commentsApiList;
  }
}
