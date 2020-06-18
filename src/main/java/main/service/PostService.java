package main.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.persistence.EntityNotFoundException;
import main.api.response.PostByIdApi;
import main.api.response.PostListApi;
import main.api.response.ResponsePostApi;
import main.api.response.ResponsePostApiToModeration;
import main.mapper.CommentMapper;
import main.mapper.PostMapper;
import main.model.Post;
import main.model.PostComment;
import main.model.Tag;
import main.model.User;
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

  @Autowired
  UserService userService;

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

  public PostListApi getAllPostsByTagAndTitle(Integer offset, Integer limit, String query)
      throws EntityNotFoundException {
    Optional<Tag> tag = tagRepository.findTagByQuery(query);
    if (!tag.isEmpty()) {
      Set<Post> posts = tagRepository.findById(tag.get().getId()).get().getPosts();
      HashSet<Post> postByQuery = postRepository.findPostByQuery(query);
      Set<Post> union = Stream.concat(posts.stream(), postByQuery.stream()).
          filter(p -> p.getTime().isBefore(LocalDateTime.now())).
          collect(Collectors.toSet());
      List<ResponsePostApi> pageApi;
      pageApi = union.stream().map(p -> postMapper.postToResponsePostApi(p)).
          collect(Collectors.toList());
      List<ResponsePostApi> responsePostApis = commentMapper
          .addCommentsCountAndLikesForPosts(pageApi);
      return new PostListApi(responsePostApis, responsePostApis.size());
    } else {
      throw new EntityNotFoundException("Nothing found");
    }
  }

  public PostListApi getAllPostsByTag(Integer offset, Integer limit, String tag) {
    Optional<Tag> tag1 = tagRepository.findTagByQuery(tag);
    if (!tag1.isEmpty()) {
      Set<Post> posts = tagRepository.findById(tag1.get().getId()).get().getPosts();
      List<ResponsePostApi> pageApi = posts.stream()
          .filter(p -> p.getIsActive() == 1 && p.getModerationStatus()
              .equals(ModerationStatus.ACCEPTED) && p.getTime().isBefore(LocalDateTime.now())).
              map(p -> postMapper.postToResponsePostApi(p)).
              collect(Collectors.toList());
      if (pageApi.size() > 0) {
        List<ResponsePostApi> responsePostApis = commentMapper
            .addCommentsCountAndLikesForPosts(pageApi);
        return new PostListApi(responsePostApis, responsePostApis.size());
      } else {
        throw new EntityNotFoundException("No active posts or moderated");
      }
    } else {
      throw new EntityNotFoundException("Nothing found");
    }
  }

  public PostListApi getAllPostsByDate(Integer offset, Integer limit, String date) {
    List<Post> allPosts = postRepository.findAll();
    List<Post> datePosts = allPosts.stream().filter(p -> p.getTime().toString().
        startsWith(date)).collect(Collectors.toList());
    if (datePosts.size() > 0) {
      List<ResponsePostApi> pageApi = datePosts.stream()
          .map(p -> postMapper.postToResponsePostApi(p))
          .collect(Collectors.toList());
      List<ResponsePostApi> responsePostApis = commentMapper
          .addCommentsCountAndLikesForPosts(pageApi);
      return new PostListApi(responsePostApis, responsePostApis.size());
    } else {
      throw new EntityNotFoundException("Nothing found");
    }
  }

  public PostByIdApi findPostById(int postId) {
    Optional<Post> optional = postRepository.findById(postId);
    if (!optional.isEmpty()) {
      PostByIdApi postByIdApi = new PostByIdApi();
      Post post = optional.get();
      if (post.getIsActive() == 1 && post.getModerationStatus().equals(ModerationStatus.ACCEPTED) &&
          post.getTime().isBefore(LocalDateTime.now())) {
        PostByIdApi postByIdApi1 = postMapper.postToPostById(post);
        postByIdApi = commentMapper.addCountCommentsAndLikesToPostById(postByIdApi1);
        List<PostComment> commentsByPostId = postCommentRepository
            .findCommentsByPostId(post.getId());
        postByIdApi.setComments(commentMapper.postCommentListToCommentApi(commentsByPostId));
        Set<Tag> tags = postRepository.findById(postId).get().getTags();
        List<String> strings = tags.stream().map(t -> t.getName())
            .collect(Collectors.toList());
        postByIdApi.setTags(strings);

      }
      return postByIdApi;
    } else {
      throw new EntityNotFoundException("Nothing found");
    }
  }

  public PostListApi getAllMyPosts(Pageable pageable, String status) throws Exception {
    User currentUser = userService.getCurrentUser();
    String email = currentUser.getEmail();
    Page<Post> allMyPosts = postRepository.findAllMyPosts(pageable, currentUser.getId());
    List<ResponsePostApi> listResponse;
    List<ResponsePostApi> listResponseApi;
    if (status.toUpperCase().equals("INACTIVE")) {
      listResponse = allMyPosts.stream().filter(p -> p.getIsActive() == 0)
          .map(p -> postMapper.postToResponsePostApiWithEmailName(p))
          .collect(Collectors.toList());
      listResponseApi = commentMapper.addCommentsCountAndLikesForPosts(listResponse);
      return new PostListApi(listResponseApi, listResponseApi.size());
    }
    if (status.toUpperCase().equals("PENDING")) {
      listResponse = allMyPosts.stream().filter(p -> p.getIsActive() == 1
          && p.getModerationStatus().equals(ModerationStatus.NEW))
          .map(p -> postMapper.postToResponsePostApiWithEmailName(p))
          .collect(Collectors.toList());
      listResponseApi = commentMapper.addCommentsCountAndLikesForPosts(listResponse);
      return new PostListApi(listResponseApi, listResponseApi.size());
    }
    if (status.toUpperCase().equals("DECLINED")) {
      listResponse = allMyPosts.stream().filter(p -> p.getIsActive() == 1
          && p.getModerationStatus().equals(ModerationStatus.DECLINED))
          .map(p -> postMapper.postToResponsePostApiWithEmailName(p))
          .collect(Collectors.toList());
      listResponseApi = commentMapper.addCommentsCountAndLikesForPosts(listResponse);
      return new PostListApi(listResponseApi, listResponseApi.size());
    }
    if (status.toUpperCase().equals("PUBLISHED")) {
      listResponse = allMyPosts.stream().filter(p -> p.getIsActive() == 1
          && p.getModerationStatus().equals(ModerationStatus.ACCEPTED))
          .map(p -> postMapper.postToResponsePostApiWithEmailName(p))
          .collect(Collectors.toList());
      listResponseApi = commentMapper.addCommentsCountAndLikesForPosts(listResponse);
      return new PostListApi(listResponseApi, listResponseApi.size());
    }
    return null;
  }

  public PostListApi getAllPostsToModeration(Pageable pageable, String status) {
    Page<ResponsePostApiToModeration> pageApiNew = postRepository
        .findAllPostsToModeration(pageable, status)
        .map(p -> postMapper.postToResponsePostApiToModeration(p));
    return new PostListApi(pageApiNew.toList(), pageApiNew.getTotalElements());
  }

  public JsonNode addPost(String time, Integer active, String title, String text, String tags)
      throws Exception {
    ObjectMapper mapper = new ObjectMapper();
    ObjectNode object = mapper.createObjectNode();
    if (title.length() >= 10 && text.length() >= 500) {
      User currentUser = userService.getCurrentUser();
      Post post = new Post();
      post.setUser(currentUser);
      post.setModerationStatus(ModerationStatus.NEW);
      post.setTitle(title);
      post.setIsActive(active);
      String[] arrayTags = tags.split(",");
      Set<Tag> setTags = Arrays.stream(arrayTags).map(t -> tagRepository.findTagByQuery(t).get())
          .collect(Collectors.toSet());
      post.setTags(setTags);
      post.setModerator(currentUser);
      post.setText(text);
      DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-d HH:mm:ss");
      LocalDateTime localDateTime = LocalDateTime.parse(time, formatter);
      if (localDateTime.isBefore(LocalDateTime.now())) {
        post.setTime(LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS));
      } else {
        post.setTime(localDateTime);
      }
      post.setView_count(0);
      postRepository.save(post);

      object.put("result", true);
    }
    if (title.length() < 10) {
      object.put("result", false);
      ObjectNode objectError = mapper.createObjectNode();
      objectError.put("title", "Заголовок не установлен или короче 10 символов");
      object.put("error", objectError);
      if (text.length() < 500) {
        objectError.put("text", "Текст публикации слишком кроткий");
        object.put("error", objectError);
      }
    }
    if (text.length() < 500 && title.length() >= 10) {
      object.put("result", false);
      ObjectNode objectError = mapper.createObjectNode();
      objectError.put("text", "Текст публикации слишком кроткий");
      object.put("error", objectError);
    }
    return object;
  }
}
