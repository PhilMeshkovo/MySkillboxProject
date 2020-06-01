package main.service;

import java.util.Comparator;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import main.api.response.PostByIdApi;
import main.api.response.PostListApi;
import main.api.response.ResponsePostApi;
import main.api.response.ResponsePostApiToModeration;
import main.mapper.CommentMapper;
import main.mapper.PostMapper;
import main.model.Post;
import main.model.PostComment;
import main.model.Tag;
import main.model.enums.ModerationStatus;
import main.repository.PostCommentRepository;
import main.repository.PostRepository;
import main.repository.PostVotesRepository;
import main.repository.TagRepository;
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

  @Autowired
  TagRepository tagRepository;

  @Autowired
  PostMapper postMapper;

  @Autowired
  CommentMapper commentMapper;

  public PostListApi getAllPosts(Pageable pageable, String mode) {
    Page<ResponsePostApi> pageApi;
    if (mode.toUpperCase().equals("RECENT")) {
      pageApi = postRepository.findAllPostsOrderedByTime(pageable)
          .map(p -> postMapper.postToResponsePostApi(p));
      Page<ResponsePostApi> pageApiNew = commentMapper.addCommentsCountAndLikes(pageApi);
      return new PostListApi(pageApiNew.toList(), pageApiNew.getTotalElements());
    }
    if (mode.toUpperCase().equals("POPULAR")) {
      pageApi = postRepository.findAll(pageable)
          .map(p -> postMapper.postToResponsePostApi(p));
      Page<ResponsePostApi> pageApiNew = commentMapper.addCommentsCountAndLikes(pageApi);
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
      Page<ResponsePostApi> pageApiNew = commentMapper.addCommentsCountAndLikes(pageApi);
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

  public PostListApi getAllPostsByTagAndTitle(Integer offset, Integer limit, String query) {
    Tag tag = tagRepository.findTagByQuery(query);
    Set<Post> posts = tagRepository.findById(tag.getId()).get().getPosts();
    HashSet<Post> postByQuery = postRepository.findPostByQuery(query);
    Set<Post> union = Stream.concat(posts.stream(), postByQuery.stream()).
        filter(p -> p.getTime().before(new Date())).
        collect(Collectors.toSet());
    List<ResponsePostApi> pageApi;
    pageApi = union.stream().map(p -> postMapper.postToResponsePostApi(p)).
        collect(Collectors.toList());
    List<ResponsePostApi> responsePostApis = commentMapper
        .addCommentsCountAndLikesForPosts(pageApi);
    return new PostListApi(responsePostApis, responsePostApis.size());
  }

  public PostListApi getAllPostsByTag(Integer offset, Integer limit, String tag) {
    Tag tag1 = tagRepository.findTagByQuery(tag);
    Set<Post> posts = tagRepository.findById(tag1.getId()).get().getPosts();
    List<ResponsePostApi> pageApi = posts.stream()
        .filter(p -> p.getIsActive() == 1 && p.getModerationStatus()
            .equals(ModerationStatus.ACCEPTED) && p.getTime().before(new Date())).
            map(p -> postMapper.postToResponsePostApi(p)).
            collect(Collectors.toList());
    List<ResponsePostApi> responsePostApis = commentMapper
        .addCommentsCountAndLikesForPosts(pageApi);
    return new PostListApi(responsePostApis, responsePostApis.size());
  }

  public PostListApi getAllPostsByDate(Integer offset, Integer limit, String date) {
    List<Post> allPosts = postRepository.findAll();
    List<Post> datePosts = allPosts.stream().filter(p -> p.getTime().toString().
        startsWith(date)).collect(Collectors.toList());
    List<ResponsePostApi> pageApi = datePosts.stream().map(p -> postMapper.postToResponsePostApi(p))
        .collect(Collectors.toList());
    List<ResponsePostApi> responsePostApis = commentMapper
        .addCommentsCountAndLikesForPosts(pageApi);
    return new PostListApi(responsePostApis, responsePostApis.size());
  }

  public PostByIdApi findPostById(int postId) {
    Optional<Post> optional = postRepository.findById(postId);
    if (!optional.isEmpty()) {
      Post post = optional.get();
      if (post.getIsActive() == 1 && post.getModerationStatus().equals(ModerationStatus.ACCEPTED) &&
          post.getTime().before(new Date())) {
        PostByIdApi postByIdApi1 = postMapper.postToPostById(post);
        PostByIdApi postByIdApi = commentMapper.addCountCommentsAndLikesToPostById(postByIdApi1);
        List<PostComment> commentsByPostId = postCommentRepository
            .findCommentsByPostId(post.getId());
        postByIdApi.setComments(commentMapper.postCommentListToCommentApi(commentsByPostId));
        Set<Tag> tags = postRepository.findById(postId).get().getTags();
        List<String> strings = tags.stream().map(t -> t.getName())
            .collect(Collectors.toList());
        postByIdApi.setTags(strings);
        return postByIdApi;
      }
    }
    return null;
  }

  public PostListApi getAllPostsToModeration(Pageable pageable, String status) {
    Page<ResponsePostApiToModeration> pageApiNew = postRepository
        .findAllPostsToModeration(pageable, status)
        .map(p -> postMapper.postToResponsePostApiToModeration(p));
    return new PostListApi(pageApiNew.toList(), pageApiNew.getTotalElements());
  }
}
