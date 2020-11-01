package main.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import javax.persistence.EntityNotFoundException;
import javax.servlet.http.HttpServletRequest;
import main.dto.request.AddPostRequest;
import main.dto.request.PostCommentRequest;
import main.dto.request.PostLikeRequest;
import main.dto.request.PostModerationRequest;
import main.dto.response.Errors;
import main.dto.response.ListTagsResponse;
import main.dto.response.PostByIdResponse;
import main.dto.response.PostListResponse;
import main.dto.response.ResponsePostApi;
import main.dto.response.ResponsePostApiWithAnnounce;
import main.dto.response.ResultResponse;
import main.dto.response.ResultResponseWithErrors;
import main.dto.response.TagResponse;
import main.mapper.CommentMapper;
import main.mapper.PostMapper;
import main.model.GlobalSettings;
import main.model.Post;
import main.model.PostComment;
import main.model.PostVotes;
import main.model.Tag;
import main.model.User;
import main.model.enums.ModerationStatus;
import main.repository.GlobalSettingsRepository;
import main.repository.PostCommentRepository;
import main.repository.PostRepository;
import main.repository.PostVotesRepository;
import main.repository.TagRepository;
import main.repository.UserRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.ResponseStatus;

@Service
public class PostService {

  @Value("${post.text.minLength}")
  private int textMin;

  @Value("${post.title.minLength}")
  private int titleMin;


  final
  PostRepository postRepository;

  final
  UserRepository userRepository;

  final
  HttpServletRequest request;

  final
  PostCommentRepository postCommentRepository;

  final
  PostVotesRepository postVotesRepository;

  final
  TagRepository tagRepository;

  final
  PostMapper postMapper;

  final
  CommentMapper commentMapper;

  final
  GlobalSettingsRepository globalSettingsRepository;

  final
  AuthenticationService authenticationService;

  public PostService(PostRepository postRepository, UserRepository userRepository,
      HttpServletRequest request, PostCommentRepository postCommentRepository,
      PostVotesRepository postVotesRepository, TagRepository tagRepository, PostMapper postMapper,
      CommentMapper commentMapper, GlobalSettingsRepository globalSettingsRepository,
      AuthenticationService authenticationService) {
    this.postRepository = postRepository;
    this.userRepository = userRepository;
    this.request = request;
    this.postCommentRepository = postCommentRepository;
    this.postVotesRepository = postVotesRepository;
    this.tagRepository = tagRepository;
    this.postMapper = postMapper;
    this.commentMapper = commentMapper;
    this.globalSettingsRepository = globalSettingsRepository;
    this.authenticationService = authenticationService;
  }

  public PostListResponse getAllPosts(Integer offset, Integer limit, String mode) {
    List<Post> posts = new ArrayList<>();
    if (mode.equalsIgnoreCase("RECENT")) {
      posts = postRepository
          .findAllPostsOrderedByTimeDesc(offset, limit);
    } else if (mode.equalsIgnoreCase("EARLY")) {
      posts = postRepository
          .findAllPostsOrderedByTime(offset, limit);
    } else if (mode.equalsIgnoreCase("POPULAR")) {
      posts = postRepository
          .findAllPostsSortedByComments(offset, limit);
    } else if (mode.equalsIgnoreCase("BEST")) {
      posts = postRepository
          .findAllPostsSortedByLikes(offset, limit);
    }
    return mapToPostListResponse(posts);
  }

  public PostListResponse getAllPostsByTextAndTitle(Integer offset, Integer limit, String query)
      throws EntityNotFoundException {
    List<Post> posts = postRepository.findPostByQuery(offset, limit, query);
    return mapToPostListResponse(posts);
  }

  public PostListResponse getAllPostsByTag(Integer offset, Integer limit, String tag) {
    Optional<Tag> tagById = tagRepository.findTagByQuery(tag);
    if (tagById.isPresent()) {
      Set<Post> posts = tagById.get().getPosts();
      List<Integer> idList = posts.stream().map(Post::getId).collect(Collectors.toList());
      List<Post> postListWithPagination = postRepository
          .findByIdIn(idList, offset, limit);
      return mapToPostListResponse(postListWithPagination);
    } else {
      throw new EntityNotFoundException("Nothing found");
    }
  }

  public PostListResponse getAllPostsByDate(Integer offset, Integer limit, String date)
      throws ParseException {
    SimpleDateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd");
    Date localDate = dateFormatter.parse(date);
    List<Post> posts = postRepository
        .findAllPostsByTime(offset, limit, localDate);
    if (posts.size() > 0) {
      return mapToPostListResponse(posts);
    } else {
      throw new EntityNotFoundException("Nothing found");
    }
  }

  @Transactional
  public PostByIdResponse findPostById(int postId) {
    Optional<Post> optionalPost = postRepository.findById(postId);
    Post post = optionalPost.orElseThrow(EntityNotFoundException::new);

    String sessionId = request.getSession().getId();
    Integer id = authenticationService.getAuthorizedUsers().get(sessionId);

    User currentUser = null;

    if (id != null) {
      currentUser = userRepository.findById(id).orElseThrow(EntityNotFoundException::new);
    }

    PostByIdResponse postByIdApi = new PostByIdResponse();

    if ((post.getIsActive() == 1 &&
        post.getModerationStatus() == ModerationStatus.ACCEPTED &&
        post.getTime().minusHours(3).isBefore(LocalDateTime.now()))
        || (currentUser != null && post.getIsActive() == 1 && post.getModerationStatus() == ModerationStatus.NEW &&
        authenticationService.getAuthorizedUsers().containsKey(sessionId)
        && currentUser.getIsModerator() == 1)
        || (currentUser != null && post.getIsActive() == 1 && post.getModerationStatus() == ModerationStatus.NEW
        && authenticationService.getAuthorizedUsers().containsKey(sessionId) && currentUser
        .equals(post.getUser()))) {
      postByIdApi = postMapper.postToPostById(post);
      List<PostComment> commentsByPostId = postCommentRepository
          .findCommentsByPostId(post.getId());
      postByIdApi.setComments(commentMapper.postCommentListToCommentApi(commentsByPostId));
      Set<Tag> tags = post.getTags();
      List<String> strings = tags.stream().map(Tag::getName)
          .collect(Collectors.toList());
      if (!authenticationService.getAuthorizedUsers().containsKey(sessionId) ||
          (authenticationService.getAuthorizedUsers().containsKey(sessionId) && currentUser != null
              && !currentUser.equals(post.getUser()))
          || (authenticationService.getAuthorizedUsers().containsKey(sessionId) && currentUser != null &&
          currentUser.getIsModerator() != 1)) {
        Post post1 = postRepository.getOne(postId);
        int viewCount = post.getViewCount() + 1;
        post1.setViewCount(viewCount);
      }
      postByIdApi.setTags(strings);
    }
    return postByIdApi;
  }

  public PostListResponse getAllMyPosts(Integer offset, Integer limit, String status) {
    String sessionId = request.getSession().getId();
    Integer id = authenticationService.getAuthorizedUsers().get(sessionId);
    User currentUser = userRepository.findById(id).orElseThrow();

    List<Post> posts = new ArrayList<>();
    if (status.toUpperCase().equals("INACTIVE")) {
      posts = postRepository.findAllMyPostsInactive(offset, limit, currentUser.getId());
    } else if (status.toUpperCase().equals("PENDING")) {
      posts = postRepository.findAllMyPosts(offset, limit, "NEW", currentUser.getId());
    } else if (status.toUpperCase().equals("DECLINED")) {
      posts = postRepository.findAllMyPosts(offset, limit, "DECLINED", currentUser.getId());
    } else if (status.toUpperCase().equals("PUBLISHED")) {
      posts = postRepository.findAllMyPosts(offset, limit, "ACCEPTED", currentUser.getId());
    }

    return mapToPostListResponse(posts);
  }

  public PostListResponse getAllPostsToModeration(Integer offset, Integer limit, String status) {
    List<Post> posts = postRepository.findAllPostsToModeration(offset, limit, status);
    return mapToPostListResponse(posts);
  }

  private PostListResponse mapToPostListResponse(List<Post> posts) {
    List<ResponsePostApi> listResponse = posts.stream()
        .map(p -> postMapper.postToResponsePostApi(p)).collect(Collectors.toList());
    List<ResponsePostApiWithAnnounce> responseWithAnnounceList =
        listResponse.stream().map(p -> postMapper.responsePostApiToResponseWithAnnounce(p)).
            collect(Collectors.toList());
    return new PostListResponse(responseWithAnnounceList, listResponse.size());
  }

  @Transactional
  public ResultResponseWithErrors addPost(AddPostRequest addPostDto) {
    GlobalSettings globalSettings = globalSettingsRepository.findByCode("POST_PREMODERATION")
        .orElseThrow(EntityNotFoundException::new);
    ResultResponseWithErrors resultResponseWithErrors = new ResultResponseWithErrors();
    Errors errors = new Errors();
    if (addPostDto.getTitle().length() >= titleMin && addPostDto.getText().length() >= textMin) {
      String sessionId = request.getSession().getId();
      Integer id = authenticationService.getAuthorizedUsers().get(sessionId);
      User currentUser = userRepository.findById(id).orElseThrow();
      LocalDateTime localDateTime = LocalDateTime
          .ofInstant(Instant.ofEpochSecond(addPostDto.getTimestamp()),
              ZoneId.systemDefault());
      localDateTime = correctTime(localDateTime);
      if (localDateTime.isAfter(correctTime(LocalDateTime.now()))) {
        localDateTime = correctTime(LocalDateTime.now());
      }
      Post post = new Post();
      post.setUser(currentUser);
      post.setTitle(addPostDto.getTitle());
      post.setIsActive(addPostDto.getActive());
      post.setModerator(currentUser);
      post.setText(addPostDto.getText());
      post.setTime(localDateTime);
      post.setViewCount(0);
      if ((globalSettings.getValue().equals("NO") && addPostDto.getActive() == 1) ||
          (globalSettings.getValue().equals("YES")
              && currentUser.getIsModerator() == 1
              && addPostDto.getActive() == 1)) {
        post.setModerationStatus((ModerationStatus.ACCEPTED));
      }

      if (globalSettings.getValue().equals("YES") && currentUser.getIsModerator() != 1) {
        post.setModerationStatus((ModerationStatus.NEW));
      }
      if (addPostDto.getTags() != null) {
        Set<Tag> setTags = new HashSet<>();
        String[] arrayTags = addPostDto.getTags();
        List<String> tagNames = tagRepository.findAll().stream().map(t -> t.getName())
            .collect(Collectors.toList());
        for (String arrayTag : arrayTags) {
          if (!tagNames.contains(arrayTag)) {
            Tag tag = new Tag();
            tag.setName(arrayTag);
            tagRepository.save(tag);
          }
          Tag tag = tagRepository.findTagByQuery(arrayTag).get();
          setTags.add(tag);
        }
        post.setTags(setTags);
      }
      postRepository.save(post);
      resultResponseWithErrors.resultSuccess();
    } else {
      if (addPostDto.getTitle().length() < titleMin) {
        errors.setTitle("Заголовок не установлен или слишком кроткий");
      }
      if (addPostDto.getText().length() < textMin) {
        errors.setText("Текст публикации слишком кроткий");
      }
      resultResponseWithErrors.setErrors(errors);
    }
    return resultResponseWithErrors;
  }

  @Transactional
  public ResultResponseWithErrors updatePost(int id, AddPostRequest addPostDto) {
    ResultResponseWithErrors resultResponseWithErrors = new ResultResponseWithErrors();
    Errors errors = new Errors();

    String sessionId = request.getSession().getId();
    Integer idUser = authenticationService.getAuthorizedUsers().get(sessionId);

    Post postById = postRepository.findById(id).orElseThrow(EntityNotFoundException::new);
    User currentUser = userRepository.findById(idUser).orElseThrow(EntityNotFoundException::new);

    if (postById.getUser()
        .equals(currentUser)) {
      if (addPostDto.getTitle().length() >= titleMin && addPostDto.getText().length() >= textMin) {

        Set<Tag> setTags = new HashSet<>();
        if (addPostDto.getTags() != null) {
          setTags = Arrays.stream(addPostDto.getTags())
              .map(t -> tagRepository.findTagByQuery(t).orElseThrow())
              .collect(Collectors.toSet());
        }
        LocalDateTime localDateTime = LocalDateTime
            .ofInstant(Instant.ofEpochSecond(addPostDto.getTimestamp()),
                ZoneId.systemDefault());
        localDateTime = correctTime(localDateTime);
        if (localDateTime.isAfter(correctTime(LocalDateTime.now()))) {
          localDateTime = correctTime(LocalDateTime.now());
        }
        Post postToUpdate = postRepository.getOne(id);
        postToUpdate.setTime(localDateTime);
        postToUpdate.setIsActive(addPostDto.getActive());
        postToUpdate.setTitle(addPostDto.getTitle());
        postToUpdate.setText(addPostDto.getText());
        postToUpdate.setTags(setTags);

        resultResponseWithErrors.resultSuccess();
      } else {
        if (addPostDto.getTitle().length() < titleMin) {
          errors.setTitle("Заголовок не установлен или слишком короткий");
        }
        if (addPostDto.getText().length() < textMin) {
          errors.setText("Текст публикации слишком кроткий");
        }
        resultResponseWithErrors.setErrors(errors);
      }
      return resultResponseWithErrors;
    } else {
      throw new EntityNotFoundException("post does not exist or it is not yours");
    }
  }

  public ResponseEntity addCommentToPost(PostCommentRequest postCommentDto) {
    ResultResponseWithErrors response = new ResultResponseWithErrors();
    Errors errors = new Errors();
    Integer postId = postCommentDto.getPostId();
    String text = postCommentDto.getText();
    Optional<Post> postById = postRepository.findById(postId);
    String sessionId = request.getSession().getId();
    Integer idUser = authenticationService.getAuthorizedUsers().get(sessionId);
    Optional<User> currentUser = userRepository.findById(idUser);
    if (currentUser.isPresent() && postById.isPresent() && text.length() > 9) {
      PostComment postComment = new PostComment();
      postComment.setPost(postById.get());
      postComment.setUser(currentUser.get());
      postComment.setTime(correctTime(LocalDateTime.now()));
      postComment.setText(text);
      if (postCommentDto.getParentId() != null) {
        Optional<PostComment> optionalParent = postCommentRepository
            .findById(postCommentDto.getParentId());
        if (optionalParent.isPresent()) {
          postComment.setParent(postComment.getParent());
        }
      }
      PostComment savedPostComment = postCommentRepository.save(postComment);
      ObjectMapper mapper = new ObjectMapper();
      ObjectNode object = mapper.createObjectNode();
      object.put("id", savedPostComment.getId());
      return new ResponseEntity<>(object, HttpStatus.OK);
    } else {
      response.setResult(false);
      if (postById.isEmpty()) {
        errors.setPost("Post not exist");
      }
      if (text.length() < 10) {
        errors.setText("Comment text too short");
      }
      if (currentUser.isEmpty()) {
        errors.setUser("No such user");
      }
      response.setErrors(errors);
    }
    return new ResponseEntity(response, HttpStatus.BAD_REQUEST);
  }

  public ListTagsResponse getTag(String query) {
    ListTagsResponse listTagsResponse = new ListTagsResponse();
    List<TagResponse> tagDtoList = new ArrayList<>();
    if (query.equals("")) {
      List<Tag> allTags = tagRepository.findAll();
      for (Tag tag : allTags) {
        TagResponse tagDto = TagResponse.builder()
            .name(tag.getName())
            .weight(getWeightOfTag(tag))
            .build();
        tagDtoList.add(tagDto);
      }
    } else {
      String[] arrayTags = query.split(",");
      for (String arrayTag : arrayTags) {
        Optional<Tag> tagByQuery = tagRepository.findTagByQuery(arrayTag);
        if (tagByQuery.isPresent()) {
          double weight = getWeightOfTag(tagByQuery.get());
          TagResponse tagDto = TagResponse.builder()
              .name(tagByQuery.get().getName())
              .weight(weight)
              .build();
          tagDtoList.add(tagDto);
        }
      }
    }
    listTagsResponse.setTags(tagDtoList);
    return listTagsResponse;
  }

  private Double getWeightOfTag(Tag tag) {
    double countActivePosts = postRepository.findAll().stream()
        .filter(p -> p.getIsActive() == 1 && p.getModerationStatus()
            == ModerationStatus.ACCEPTED && p.getTime().isBefore(LocalDateTime.now()))
        .count();
    double countPostsWithThisTag = tag.getPosts().stream()
        .filter(p -> p.getIsActive() == 1 && p.getModerationStatus()
            == ModerationStatus.ACCEPTED && p.getTime().isBefore(LocalDateTime.now()))
        .count();
    List<Tag> allTags = tagRepository.findAll();
    double maxPostsTag = 0.0;
    for (Tag tags : allTags) {
      double activePosts = tags.getPosts().stream()
          .filter(p -> p.getIsActive() == 1 && p.getModerationStatus()
              == ModerationStatus.ACCEPTED && p.getTime().isBefore(LocalDateTime.now()))
          .count();
      if (activePosts > maxPostsTag) {
        maxPostsTag = activePosts;
      }
    }
    double coefficient = countActivePosts / maxPostsTag;
    return (countPostsWithThisTag / countActivePosts) * coefficient;
  }

  @Transactional
  public ResultResponse moderationPost(PostModerationRequest postModerationDto) {
    ResultResponse resultResponse = new ResultResponse();
    Post postById = postRepository.findById(postModerationDto.getPostId())
        .orElseThrow(EntityNotFoundException::new);
      Post postToModeration = postRepository.getOne(postById.getId());
      if (postModerationDto.getDecision().equalsIgnoreCase("accept")) {
        postToModeration.setModerationStatus(ModerationStatus.ACCEPTED);
      }
      if (postModerationDto.getDecision().equalsIgnoreCase("decline")) {
        postToModeration.setModerationStatus(ModerationStatus.DECLINED);
      }
      String sessionId = request.getSession().getId();
      Integer id = authenticationService.getAuthorizedUsers().get(sessionId);
      User currentUser = userRepository.findById(id).orElseThrow();
      postToModeration.setModerator(currentUser);
      resultResponse.resultSuccess();
    return resultResponse;
  }

  public JsonNode getAllPostsInYear(String year) {
    List<Post> allPosts = postRepository.findAll();
    List<Post> allPostsInYear;
    if (year.equals("") || !year.matches("\\d{4}")) {
      String yearNow = String.valueOf(LocalDateTime.now().getYear());
      allPostsInYear = allPosts.stream().filter(p -> p.getTime().toString().startsWith(yearNow))
          .collect(Collectors.toList());
    } else {
      allPostsInYear = allPosts.stream().filter(p -> p.getTime().toString().startsWith(year))
          .collect(Collectors.toList());
    }
    Set<Integer> allYears = allPosts.stream().map(p -> p.getTime().getYear())
        .collect(Collectors.toSet());
    List<Integer> years = allYears.stream().sorted(Comparator.reverseOrder())
        .collect(Collectors.toList());
    Map<String, Integer> dateToCountPosts = new HashMap<>();
    for (Post post : allPostsInYear) {
      Integer countPosts = Math.toIntExact(
          allPostsInYear.stream().filter(p -> p.getTime().toString().substring(0, 10)
              .equals(post.getTime().toString().substring(0, 10))).count());
      dateToCountPosts.put(post.getTime().toString().substring(0, 10), countPosts);
    }
    ObjectMapper mapper = new ObjectMapper();
    ArrayNode array = mapper.valueToTree(years);
    JsonNode map = mapper.valueToTree(dateToCountPosts);
    ObjectNode object = mapper.createObjectNode();
    object.putArray("years").addAll(array);
    object.put("posts", map);
    return object;
  }

  @Transactional
  public ResultResponse postLike(PostLikeRequest postLikeDto) throws EntityNotFoundException {
    ResultResponse resultResponse = new ResultResponse();
    Map<String, Integer> authUsers = authenticationService.getAuthorizedUsers();
    User currentUser;
    if (authUsers.containsKey(request.getSession().getId())) {
      String sessionId = request.getSession().getId();
      Integer id = authenticationService.getAuthorizedUsers().get(sessionId);
      Optional<User> optionalUser = userRepository.findById(id);
      currentUser = optionalUser.orElseThrow(EntityNotFoundException::new);
    } else {
      throw new EntityNotFoundException("User not authorized");
    }
    Optional<Post> post = postRepository.findById(postLikeDto.getPostId());
    Optional<PostVotes> postVotesOptional = postVotesRepository
        .findByPostIdAndUserId(postLikeDto.getPostId(), currentUser.getId());
    if (post.isPresent() && postVotesOptional.isEmpty()) {
      PostVotes postVotes = PostVotes.builder()
          .time(correctTime(LocalDateTime.now()))
          .value(1)
          .post(post.get())
          .user(currentUser)
          .build();
      postVotesRepository.save(postVotes);
      resultResponse.resultSuccess();
    }
    if (post.isPresent() && postVotesOptional.isPresent()
        && postVotesOptional.get().getValue() == -1) {
      PostVotes postVotes = postVotesRepository.getOne(postVotesOptional.get().getId());
      postVotes.setValue(1);
      resultResponse.resultSuccess();
    }
    return resultResponse;
  }

  @Transactional
  public ResultResponse postDislike(PostLikeRequest postLikeDto) throws EntityNotFoundException {
    ResultResponse resultResponse = new ResultResponse();
    Map<String, Integer> authUsers = authenticationService.getAuthorizedUsers();
    User currentUser;
    if (authUsers.containsKey(request.getSession().getId())) {
      String sessionId = request.getSession().getId();
      Integer id = authenticationService.getAuthorizedUsers().get(sessionId);
      Optional<User> optionalUser = userRepository.findById(id);
      currentUser = optionalUser.orElseThrow(EntityNotFoundException::new);
    } else {
      throw new EntityNotFoundException("User not authorized");
    }
    Optional<Post> post = postRepository.findById(postLikeDto.getPostId());
    Optional<PostVotes> postVotesOptional = postVotesRepository
        .findByPostIdAndUserId(postLikeDto.getPostId(), currentUser.getId());
    if (post.isPresent() && postVotesOptional.isEmpty()) {
      PostVotes postVotes = PostVotes.builder()
          .time(correctTime(LocalDateTime.now()))
          .value(-1)
          .post(post.get())
          .user(currentUser)
          .build();
      postVotesRepository.save(postVotes);
      resultResponse.resultSuccess();
    }
    if (post.isPresent() && postVotesOptional.isPresent()
        && postVotesOptional.get().getValue() == 1) {
      PostVotes postVotes = postVotesRepository.getOne(postVotesOptional.get().getId());
      postVotes.setValue(-1);
      resultResponse.resultSuccess();
    }
    return resultResponse;
  }

  private LocalDateTime correctTime(LocalDateTime time) {
    return time.plusHours(3L);
  }
}
