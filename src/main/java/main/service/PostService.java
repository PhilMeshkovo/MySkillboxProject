package main.service;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import main.api.response.PostListApi;
import main.api.response.ResponsePostApi;
import main.mapper.PostMapper;
import main.repository.PostCommentRepository;
import main.repository.PostRepository;
import main.repository.PostVotesRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
public class PostService {

  @Autowired
  PostRepository postRepository;

  @Autowired
  PostCommentRepository postCommentRepository;

  @Autowired
  PostVotesRepository postVotesRepository;

  PostMapper postMapper = new PostMapper();

  public PostListApi getAllPosts(Pageable pageable, String mode) {
    Page<ResponsePostApi> pageApi;
    if (mode.toUpperCase().equals("RECENT")) {
      pageApi = postRepository.findAllPostsOrderedByTime(pageable)
          .map(p -> postMapper.postToResponsePostApi(p));
      Page<ResponsePostApi> pageApiNew = addCommentsCountAndLikes(pageApi);
      return new PostListApi(pageApiNew.toList(), pageApiNew.getTotalElements());
    }
    if (mode.toUpperCase().equals("POPULAR")) {
      pageApi = postRepository.findAll(pageable)
          .map(p -> postMapper.postToResponsePostApi(p));
      Page<ResponsePostApi> pageApiNew = addCommentsCountAndLikes(pageApi);
      List<ResponsePostApi> sortedPageApi = pageApiNew.stream()
          .sorted(new Comparator<ResponsePostApi>() {
            @Override
            public int compare(ResponsePostApi o1, ResponsePostApi o2) {
              if (o1.getCommentCount() == o2.getCommentCount()) {
                return 0;
              } else if (o1.getCommentCount() > o2.getCommentCount()) {
                return -1;
              } else {
                return 1;
              }
            }
          }).collect(Collectors.toList());
      return new PostListApi(sortedPageApi, pageApiNew.getTotalElements());
    }
    if (mode.toUpperCase().equals("BEST")) {
      pageApi = postRepository.findAll(pageable)
          .map(p -> postMapper.postToResponsePostApi(p));
      Page<ResponsePostApi> pageApiNew = addCommentsCountAndLikes(pageApi);
      List<ResponsePostApi> sortedPageApi = pageApiNew.stream()
          .sorted(new Comparator<ResponsePostApi>() {
            @Override
            public int compare(ResponsePostApi o1, ResponsePostApi o2) {
              if (o1.getLikeCount() == o2.getLikeCount()) {
                return 0;
              } else if (o1.getLikeCount() > o2.getLikeCount()) {
                return -1;
              } else {
                return 1;
              }
            }
          }).collect(Collectors.toList());
      return new PostListApi(sortedPageApi, pageApiNew.getTotalElements());
    }
    return null;
  }

  public Page<ResponsePostApi> addCommentsCountAndLikes(Page<ResponsePostApi> pageApi) {
    for (ResponsePostApi responsePostApi : pageApi) {
      int countComments = postCommentRepository.findAll().stream()
          .filter(p -> p.getPost().getId() == responsePostApi.getId()).
              collect(Collectors.toList()).size();
      responsePostApi.setCommentCount(countComments);
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
