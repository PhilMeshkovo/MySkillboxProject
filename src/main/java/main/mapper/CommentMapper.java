package main.mapper;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import main.dto.response.CommentsApiResponse;
import main.dto.response.PostByIdResponse;
import main.dto.response.ResponsePostApi;
import main.model.PostComment;
import main.repository.PostVotesRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Component;

@Component
public class CommentMapper {

  @Autowired
  UserMapper userMapper;

  @Autowired
  PostVotesRepository postVotesRepository;

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

  public Page<ResponsePostApi> addCommentsCountAndLikes(Page<ResponsePostApi> pageApi) {
    for (ResponsePostApi responsePostApi : pageApi) {
      int countLikes = postVotesRepository.findAll().stream()
          .filter(p -> p.getValue() == 1 && p.getPost().getId() == responsePostApi.getId()).
              collect(Collectors.toList()).size();
      responsePostApi.setLikeCount(countLikes);
      int countDislikes = postVotesRepository.findAll().stream()
          .filter(p -> p.getValue() == -1 && p.getPost().getId() == responsePostApi.getId()).
              collect(Collectors.toList()).size();
      responsePostApi.setDislikeCount(countDislikes);
    }
    return pageApi;
  }

  public PostByIdResponse addCountCommentsAndLikesToPostById(PostByIdResponse postByIdApi) {
    int countLikes = postVotesRepository.findAll().stream()
        .filter(p -> p.getValue() == 1 && p.getPost().getId() == postByIdApi.getId()).
            collect(Collectors.toList()).size();
    postByIdApi.setLikeCount(countLikes);
    int countDislikes = postVotesRepository.findAll().stream()
        .filter(p -> p.getValue() == -1 && p.getPost().getId() == postByIdApi.getId()).
            collect(Collectors.toList()).size();
    postByIdApi.setDislikeCount(countDislikes);

    return postByIdApi;
  }

  public List<ResponsePostApi> addCommentsCountAndLikesForPosts(List<ResponsePostApi> pageApi) {
    for (ResponsePostApi responsePostApi : pageApi) {
      int countLikes = postVotesRepository.findAll().stream()
          .filter(p -> p.getValue() == 1 && p.getPost().getId() == responsePostApi.getId()).
              collect(Collectors.toList()).size();
      responsePostApi.setLikeCount(countLikes);
      int countDislikes = postVotesRepository.findAll().stream()
          .filter(p -> p.getValue() == -1 && p.getPost().getId() == responsePostApi.getId()).
              collect(Collectors.toList()).size();
      responsePostApi.setDislikeCount(countDislikes);
    }
    return pageApi;
  }
}
