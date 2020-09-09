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
import main.dto.response.ListTagsResponse;
import main.dto.response.PostByIdResponse;
import main.dto.response.PostListResponse;
import main.dto.response.ResponsePostApi;
import main.dto.response.ResponsePostApiWithAnnounce;
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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PostService {

  @Autowired
  PostRepository postRepository;

  @Autowired
  UserRepository userRepository;

  @Autowired
  HttpServletRequest request;

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
  GlobalSettingsRepository globalSettingsRepository;

  @Autowired
  AuthenticationService authenticationService;

  public PostListResponse getAllPosts(Integer offset, Integer limit, String mode) {
    List<ResponsePostApi> responsePostApiList;
    if (mode.equalsIgnoreCase("RECENT")) {
      responsePostApiList = postRepository
          .findAllPostsOrderedByTimeDesc(offset, limit).stream()
          .map(p -> postMapper.postToResponsePostApi(p)).collect(Collectors.toList());
      List<ResponsePostApi> pageApiNew = commentMapper
          .addCommentsCountAndLikesForPosts(responsePostApiList);
      List<ResponsePostApiWithAnnounce> responseWithAnnounceList =
          pageApiNew.stream().map(p -> postMapper.responsePostApiToResponseWithAnnounce(p)).
              collect(Collectors.toList());
      return new PostListResponse(responseWithAnnounceList, pageApiNew.size());
    }
    if (mode.equalsIgnoreCase("EARLY")) {
      responsePostApiList = postRepository
          .findAllPostsOrderedByTime(offset, limit).stream()
          .map(p -> postMapper.postToResponsePostApi(p)).collect(Collectors.toList());
      List<ResponsePostApi> pageApiNew = commentMapper
          .addCommentsCountAndLikesForPosts(responsePostApiList);
      List<ResponsePostApiWithAnnounce> responseWithAnnounceList =
          pageApiNew.stream().map(p -> postMapper.responsePostApiToResponseWithAnnounce(p)).
              collect(Collectors.toList());
      return new PostListResponse(responseWithAnnounceList, pageApiNew.size());
    }
    if (mode.equalsIgnoreCase("POPULAR")) {
      responsePostApiList = postRepository
          .findAllPostsSortedByComments(offset, limit).stream()
          .map(p -> postMapper.postToResponsePostApi(p)).collect(Collectors.toList());
      List<ResponsePostApi> pageApiNew = commentMapper
          .addCommentsCountAndLikesForPosts(responsePostApiList);
      List<ResponsePostApiWithAnnounce> responseWithAnnounceList =
          pageApiNew
              .stream().map(p -> postMapper.responsePostApiToResponseWithAnnounce(p)).
              collect(Collectors.toList());
      return new PostListResponse(responseWithAnnounceList, pageApiNew.size());
    }
    if (mode.equalsIgnoreCase("BEST")) {
      responsePostApiList = postRepository
          .findAllPostsSortedByLikes(offset, limit).stream()
          .map(p -> postMapper.postToResponsePostApi(p)).collect(Collectors.toList());
      List<ResponsePostApi> pageApiNew = commentMapper
          .addCommentsCountAndLikesForPosts(responsePostApiList);
      List<ResponsePostApiWithAnnounce> responseWithAnnounceList =
          pageApiNew.stream().map(p -> postMapper.responsePostApiToResponseWithAnnounce(p)).
              collect(Collectors.toList());
      return new PostListResponse(responseWithAnnounceList, pageApiNew.size());
    }
    throw new EntityNotFoundException("No such mode");
  }

  public PostListResponse getAllPostsByTextAndTitle(Integer offset, Integer limit, String query)
      throws EntityNotFoundException {
    List<Post> postByQuery = postRepository.findPostByQuery(offset, limit, query);
    List<ResponsePostApi> pageApi;
    pageApi = postByQuery.stream().map(p -> postMapper.postToResponsePostApi(p)).
        collect(Collectors.toList());
    List<ResponsePostApi> responsePostApis = commentMapper
        .addCommentsCountAndLikesForPosts(pageApi);
    List<ResponsePostApiWithAnnounce> responseWithAnnounceList =
        responsePostApis.stream().map(p -> postMapper.responsePostApiToResponseWithAnnounce(p)).
            collect(Collectors.toList());
    return new PostListResponse(responseWithAnnounceList, responsePostApis.size());
  }

  public PostListResponse getAllPostsByTag(Integer offset, Integer limit, String tag) {
    Optional<Tag> tagById = tagRepository.findTagByQuery(tag);
    if (tagById.isPresent()) {
      Set<Post> posts = tagById.get().getPosts();
      List<Integer> idList = posts.stream().map(p -> p.getId()).collect(Collectors.toList());
      List<Post> postListWithPagination = postRepository
          .findByIdIn(idList, offset, limit);
      List<ResponsePostApi> pageApi = postListWithPagination.stream()
          .map(p -> postMapper.postToResponsePostApi(p)).
              collect(Collectors.toList());
      if (pageApi.size() > 0) {
        List<ResponsePostApi> responsePostApis = commentMapper
            .addCommentsCountAndLikesForPosts(pageApi);
        List<ResponsePostApiWithAnnounce> responseWithAnnounceList =
            responsePostApis.stream().map(p -> postMapper.responsePostApiToResponseWithAnnounce(p)).
                collect(Collectors.toList());
        return new PostListResponse(responseWithAnnounceList, responsePostApis.size());
      } else {
        throw new EntityNotFoundException("No active posts or moderated");
      }
    } else {
      throw new EntityNotFoundException("Nothing found");
    }
  }

  public PostListResponse getAllPostsByDate(Integer offset, Integer limit, String date)
      throws ParseException {
    SimpleDateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd");
    Date localDate = dateFormatter.parse(date);
    List<Post> datePosts = postRepository
        .findAllPostsByTime(offset, limit, localDate);
    if (datePosts.size() > 0) {
      List<ResponsePostApi> pageApi = datePosts.stream()
          .map(p -> postMapper.postToResponsePostApi(p))
          .collect(Collectors.toList());
      List<ResponsePostApi> responsePostApis = commentMapper
          .addCommentsCountAndLikesForPosts(pageApi);
      List<ResponsePostApiWithAnnounce> responseWithAnnounceList =
          responsePostApis.stream().map(p -> postMapper.responsePostApiToResponseWithAnnounce(p)).
              collect(Collectors.toList());
      return new PostListResponse(responseWithAnnounceList, responsePostApis.size());
    } else {
      throw new EntityNotFoundException("Nothing found");
    }
  }

  @Transactional
  public PostByIdResponse findPostById(int postId) {
    Optional<Post> optionalPost = postRepository.findById(postId);
    String sessionId = request.getSession().getId();
    Integer id = authenticationService.getAuthorizedUsers().get(sessionId);
    if (optionalPost.isPresent()) {
      PostByIdResponse postByIdApi = new PostByIdResponse();
      Post post = optionalPost.get();
      if (post.getIsActive() == 1 && post.getModerationStatus().equals(ModerationStatus.ACCEPTED) &&
          post.getTime().minusHours(3).isBefore(LocalDateTime.now()) || post.getIsActive() != 1 &&
          authenticationService.getAuthorizedUsers().containsKey(sessionId)
          && userRepository.findById(id).get().getIsModerator() == 1
          || post.getIsActive() != 1
          && authenticationService.getAuthorizedUsers().containsKey(sessionId) && userRepository
          .findById(id).get().equals(post.getUser())) {
        PostByIdResponse postByIdApi1 = postMapper.postToPostById(post);
        postByIdApi = commentMapper.addCountCommentsAndLikesToPostById(postByIdApi1);
        List<PostComment> commentsByPostId = postCommentRepository
            .findCommentsByPostId(post.getId());
        postByIdApi.setComments(commentMapper.postCommentListToCommentApi(commentsByPostId));
        Set<Tag> tags = postRepository.findById(postId).get().getTags();
        List<String> strings = tags.stream().map(t -> t.getName())
            .collect(Collectors.toList());
        if (!authenticationService.getAuthorizedUsers().containsKey(sessionId) ||
            authenticationService.getAuthorizedUsers().containsKey(sessionId)
                && !userRepository.findById(id).get().equals(post.getUser())
            || authenticationService.getAuthorizedUsers().containsKey(sessionId) &&
            userRepository.findById(id).get().getIsModerator() != 1) {
          Post post1 = postRepository.getOne(postId);
          int viewCount = post.getView_count() + 1;
          post1.setView_count(viewCount);
        }
        postByIdApi.setTags(strings);
      }
      return postByIdApi;
    } else {
      throw new EntityNotFoundException("Nothing found");
    }
  }

  public PostListResponse getAllMyPosts(Integer offset, Integer limit, String status) {
    String sessionId = request.getSession().getId();
    Integer id = authenticationService.getAuthorizedUsers().get(sessionId);
    User currentUser = userRepository.findById(id).get();
    List<ResponsePostApi> listResponse;
    List<ResponsePostApi> listResponseApi;
    if (status.toUpperCase().equals("INACTIVE")) {
      listResponse = postRepository.findAllMyPostsInactive(offset, limit, currentUser.getId())
          .stream()
          .map(p -> postMapper.postToResponsePostApi(p))
          .collect(Collectors.toList());
      listResponseApi = commentMapper.addCommentsCountAndLikesForPosts(listResponse);
      List<ResponsePostApiWithAnnounce> responseWithAnnounceList =
          listResponseApi.stream().map(p -> postMapper.responsePostApiToResponseWithAnnounce(p)).
              collect(Collectors.toList());
      return new PostListResponse(responseWithAnnounceList, listResponseApi.size());
    }
    if (status.toUpperCase().equals("PENDING")) {
      listResponse = postRepository.findAllMyPosts(offset, limit, "NEW", currentUser.getId())
          .stream()
          .map(p -> postMapper.postToResponsePostApi(p))
          .collect(Collectors.toList());
      listResponseApi = commentMapper.addCommentsCountAndLikesForPosts(listResponse);
      List<ResponsePostApiWithAnnounce> responseWithAnnounceList =
          listResponseApi.stream().map(p -> postMapper.responsePostApiToResponseWithAnnounce(p)).
              collect(Collectors.toList());
      return new PostListResponse(responseWithAnnounceList, listResponseApi.size());
    }
    if (status.toUpperCase().equals("DECLINED")) {
      listResponse = postRepository.findAllMyPosts(offset, limit, "DECLINED", currentUser.getId())
          .stream()
          .map(p -> postMapper.postToResponsePostApi(p))
          .collect(Collectors.toList());
      listResponseApi = commentMapper.addCommentsCountAndLikesForPosts(listResponse);
      List<ResponsePostApiWithAnnounce> responseWithAnnounceList =
          listResponseApi.stream().map(p -> postMapper.responsePostApiToResponseWithAnnounce(p)).
              collect(Collectors.toList());
      return new PostListResponse(responseWithAnnounceList, listResponseApi.size());
    }
    if (status.toUpperCase().equals("PUBLISHED")) {
      listResponse = postRepository.findAllMyPosts(offset, limit, "ACCEPTED", currentUser.getId())
          .stream()
          .map(p -> postMapper.postToResponsePostApi(p))
          .collect(Collectors.toList());
      listResponseApi = commentMapper.addCommentsCountAndLikesForPosts(listResponse);
      List<ResponsePostApiWithAnnounce> responseWithAnnounceList =
          listResponseApi.stream().map(p -> postMapper.responsePostApiToResponseWithAnnounce(p)).
              collect(Collectors.toList());
      return new PostListResponse(responseWithAnnounceList, listResponseApi.size());
    }
    return null;
  }

  public PostListResponse getAllPostsToModeration(Integer offset, Integer limit, String status) {
    List<Post> posts = postRepository.findAllPostsToModeration(offset, limit, status);
    List<ResponsePostApi> postApiNew = posts.stream()
        .map(p -> postMapper.postToResponsePostApi(p)).collect(Collectors.toList());
    List<ResponsePostApi> responsePosts = commentMapper
        .addCommentsCountAndLikesForPosts(postApiNew);
    List<ResponsePostApiWithAnnounce> responseWithAnnounceList =
        responsePosts.stream().map(p -> postMapper.responsePostApiToResponseWithAnnounce(p)).
            collect(Collectors.toList());
    return new PostListResponse(responseWithAnnounceList, responsePosts.size());
  }

  public JsonNode addPost(AddPostRequest addPostDto) {
    Optional<GlobalSettings> globalSettings = globalSettingsRepository.findById(2);
    ObjectMapper mapper = new ObjectMapper();
    ObjectNode object = mapper.createObjectNode();
    if (addPostDto.getTitle().length() >= 10 && addPostDto.getText().length() >= 500) {
      String sessionId = request.getSession().getId();
      Integer id = authenticationService.getAuthorizedUsers().get(sessionId);
      User currentUser = userRepository.findById(id).get();
      Set<Tag> setTags = new HashSet<>();
      if (addPostDto.getTags() != null) {
        setTags = Arrays.stream(addPostDto.getTags())
            .map(t -> tagRepository.findTagByQuery(t).get())
            .collect(Collectors.toSet());
      }
      LocalDateTime localDateTime = LocalDateTime
          .ofInstant(Instant.ofEpochSecond(addPostDto.getTimestamp()),
              ZoneId.systemDefault());
      if (localDateTime.isAfter(LocalDateTime.now().plusHours(3L))) {
        localDateTime = LocalDateTime.now().plusHours(3L);
      }
      Post post = new Post();
      if ((globalSettings.get().getValue().equals("NO") && addPostDto.getActive() == 1) ||
          (globalSettings.get().getValue().equals("YES") && currentUser.getIsModerator() == 1
              && addPostDto.getActive() == 1)) {
        post = Post.builder()
            .user(currentUser)
            .moderationStatus(ModerationStatus.ACCEPTED)
            .title(addPostDto.getTitle())
            .isActive(addPostDto.getActive())
            .tags(setTags)
            .moderator(currentUser)
            .text(addPostDto.getText())
            .time(localDateTime.plusHours(3L))
            .view_count(0)
            .build();
      }
      if (globalSettings.get().getValue().equals("YES") && currentUser.getIsModerator() != 1) {
        post = Post.builder()
            .user(currentUser)
            .moderationStatus(ModerationStatus.NEW)
            .title(addPostDto.getTitle())
            .isActive(addPostDto.getActive())
            .tags(setTags)
            .moderator(currentUser)
            .text(addPostDto.getText())
            .time(localDateTime.plusHours(3L))
            .view_count(0)
            .build();
      }
      postRepository.save(post);

      object.put("result", true);
    }
    if (addPostDto.getTitle().length() < 10) {
      object.put("result", false);
      ObjectNode objectError = mapper.createObjectNode();
      objectError.put("title", "Заголовок не установлен или короче 10 символов");
      object.put("error", objectError);
      if (addPostDto.getText().length() < 500) {
        objectError.put("text", "Текст публикации слишком кроткий");
        object.put("error", objectError);
      }
    }
    if (addPostDto.getText().length() < 500 && addPostDto.getTitle().length() >= 10) {
      object.put("result", false);
      ObjectNode objectError = mapper.createObjectNode();
      objectError.put("text", "Текст публикации слишком кроткий");
      object.put("error", objectError);
    }
    return object;
  }

  @Transactional
  public JsonNode updatePost(int id, AddPostRequest addPostDto) {
    ObjectMapper mapper = new ObjectMapper();
    ObjectNode object = mapper.createObjectNode();
    String sessionId = request.getSession().getId();
    Integer idUser = authenticationService.getAuthorizedUsers().get(sessionId);
    User currentUser = userRepository.findById(idUser).get();
    if (addPostDto.getTitle().length() >= 10 && addPostDto.getText().length() >= 500) {
      Optional<Post> postById = postRepository.findById(id);
      if (postById.isPresent() && postById.get().getUser()
          .equals(currentUser)) {
        Set<Tag> setTags = new HashSet<>();
        if (addPostDto.getTags() != null) {
          setTags = Arrays.stream(addPostDto.getTags())
              .map(t -> tagRepository.findTagByQuery(t).get())
              .collect(Collectors.toSet());
        }
        LocalDateTime localDateTime = LocalDateTime
            .ofInstant(Instant.ofEpochSecond(addPostDto.getTimestamp()),
                ZoneId.systemDefault());
        if (localDateTime.isAfter(LocalDateTime.now().plusHours(3L))) {
          localDateTime = LocalDateTime.now().plusHours(3L);
        }
        Post postToUpdate = postRepository.getOne(id);
        postToUpdate.setTime(localDateTime.plusHours(3));
        postToUpdate.setIsActive(addPostDto.getActive());
        postToUpdate.setTitle(addPostDto.getTitle());
        postToUpdate.setText(addPostDto.getText());
        postToUpdate.setTags(setTags);

        object.put("result", true);
      } else {
        throw new EntityNotFoundException("post does not exist or it is not yours");
      }
    }
    if (addPostDto.getTitle().length() < 10) {
      object.put("result", false);
      ObjectNode objectError = mapper.createObjectNode();
      objectError.put("title", "Заголовок не установлен или короче 10 символов");
      object.put("error", objectError);
      if (addPostDto.getText().length() < 500) {
        objectError.put("text", "Текст публикации слишком кроткий");
        object.put("error", objectError);
      }
    }
    if (addPostDto.getText().length() < 500 && addPostDto.getTitle().length() >= 10) {
      object.put("result", false);
      ObjectNode objectError = mapper.createObjectNode();
      objectError.put("text", "Текст публикации слишком кроткий");
      object.put("error", objectError);
    }
    return object;
  }

  public JsonNode addCommentToPost(PostCommentRequest postCommentDto) {
    ObjectMapper mapper = new ObjectMapper();
    ObjectNode object = mapper.createObjectNode();
    Integer postId = postCommentDto.getPost_id();
    String text = postCommentDto.getText();
    Optional<Post> postById = postRepository.findById(postId);
    String sessionId = request.getSession().getId();
    Integer idUser = authenticationService.getAuthorizedUsers().get(sessionId);
    Optional<User> currentUser = userRepository.findById(idUser);
    if (currentUser.isPresent()) {
      if (postById.isPresent() && text.length() > 10 && postCommentDto.getParent_id() != null
          && postCommentRepository.findById(postCommentDto.getParent_id()).isPresent()
          && postCommentRepository.findById(postCommentDto.getParent_id()).get().getPost()
          .equals(postById.get())) {
        PostComment parent = postCommentRepository.findById(postCommentDto.getParent_id()).get();
        PostComment postComment = PostComment.builder()
            .post(postById.get())
            .parent(parent)
            .user(currentUser.get())
            .time(LocalDateTime.now())
            .text(text)
            .build();
        PostComment savedPostComment = postCommentRepository.save(postComment);
        object.put("id", savedPostComment.getId());
      }
      if (postById.isPresent() && text.length() > 9 && postCommentDto.getParent_id() == null) {
        PostComment postComment = PostComment.builder()
            .post(postById.get())
            .user(currentUser.get())
            .time(LocalDateTime.now())
            .text(text)
            .build();
        PostComment savedPostComment = postCommentRepository.save(postComment);
        object.put("id", savedPostComment.getId());
      }
      if (postCommentDto.getParent_id() != null && postById.isPresent() && !postCommentRepository
          .findById(postCommentDto.getParent_id()).get().getPost().equals(postById.get())) {
        object.put("result", false);
        ObjectNode objectError = mapper.createObjectNode();
        objectError.put("parent", "No parent comment on this post");
        object.put("error", objectError);
      }
      if (postById.isEmpty()) {
        object.put("result", false);
        ObjectNode objectError = mapper.createObjectNode();
        objectError.put("post", "Post not exist");
        object.put("error", objectError);
        if (text.length() < 10) {
          objectError.put("text", "Comment text too short");
          object.put("error", objectError);
        }
      }
      if (postById.isPresent() && text.length() < 10) {
        object.put("result", false);
        ObjectNode objectError = mapper.createObjectNode();
        objectError.put("text", "Comment text too short");
        object.put("error", objectError);
      }

      return object;
    } else {
      throw new EntityNotFoundException("User not authorized");
    }
  }

  public ListTagsResponse getTag(String query) {
    ListTagsResponse listTagsDto = new ListTagsResponse();
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
      listTagsDto.setTags(tagDtoList);
      return listTagsDto;
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
        } else {
          throw new EntityNotFoundException("tag '" + arrayTag + "' - does not exist");
        }
      }
      listTagsDto.setTags(tagDtoList);
      return listTagsDto;
    }
  }

  private Double getWeightOfTag(Tag tag) {
    double countActivePosts = postRepository.findAll().stream()
        .filter(p -> p.getIsActive() == 1 && p.getModerationStatus()
            .equals(ModerationStatus.ACCEPTED) && p.getTime().isBefore(LocalDateTime.now()))
        .count();
    double countPostsWithThisTag = tag.getPosts().stream()
        .filter(p -> p.getIsActive() == 1 && p.getModerationStatus()
            .equals(ModerationStatus.ACCEPTED) && p.getTime().isBefore(LocalDateTime.now()))
        .count();
    List<Tag> allTags = tagRepository.findAll();
    double maxPostsTag = 0.0;
    for (Tag tags : allTags) {
      double activePosts = tags.getPosts().stream()
          .filter(p -> p.getIsActive() == 1 && p.getModerationStatus()
              .equals(ModerationStatus.ACCEPTED) && p.getTime().isBefore(LocalDateTime.now()))
          .count();
      if (activePosts > maxPostsTag) {
        maxPostsTag = activePosts;
      }
    }
    double coefficient = countActivePosts / maxPostsTag;
    double weight = (countPostsWithThisTag / countActivePosts) * coefficient;
    return weight;
  }

  @Transactional
  public boolean moderationPost(PostModerationRequest postModerationDto) throws Exception {
    Optional<Post> postById = postRepository.findById(postModerationDto.getPost_id());
    if (postById.isPresent() && postModerationDto.getDecision().equalsIgnoreCase("accept")
        || postById.isPresent() && postModerationDto.getDecision().equalsIgnoreCase("decline")) {
      Post postToModeration = postRepository.getOne(postModerationDto.getPost_id());
      if (postModerationDto.getDecision().equalsIgnoreCase("accept")) {
        postToModeration.setModerationStatus(ModerationStatus.ACCEPTED);
      }
      if (postModerationDto.getDecision().equalsIgnoreCase("decline")) {
        postToModeration.setModerationStatus(ModerationStatus.DECLINED);
      }
      String sessionId = request.getSession().getId();
      Integer id = authenticationService.getAuthorizedUsers().get(sessionId);
      User currentUser = userRepository.findById(id).get();
      postToModeration.setModerator(currentUser);
      return true;
    } else {
      throw new Exception("post does not exist or decision is impossible");
    }
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
  public JsonNode postLike(PostLikeRequest postLikeDto) throws EntityNotFoundException {
    ObjectNode object = createObjectNode();
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
    Optional<Post> post = postRepository.findById(postLikeDto.getPost_id());
    Optional<PostVotes> postVotesOptional = postVotesRepository
        .findByPostIdAndUserId(postLikeDto.getPost_id(), currentUser.getId());
    if (post.isEmpty()
        || postVotesOptional.isPresent() && postVotesOptional.get().getValue() == 1) {
      object.put("result", false);
    }
    if (post.isPresent() && postVotesOptional.isEmpty()) {
      PostVotes postVotes = PostVotes.builder()
          .time(LocalDateTime.now())
          .value(1)
          .post(post.get())
          .user(currentUser)
          .build();
      postVotesRepository.save(postVotes);
      object.put("result", true);
    }
    if (post.isPresent() && postVotesOptional.isPresent()
        && postVotesOptional.get().getValue() == -1) {
      PostVotes postVotes = postVotesRepository.getOne(postVotesOptional.get().getId());
      postVotes.setValue(1);
      object.put("result", true);
    }
    return object;
  }

  @Transactional
  public JsonNode postDislike(PostLikeRequest postLikeDto) throws EntityNotFoundException {
    ObjectNode object = createObjectNode();
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
    Optional<Post> post = postRepository.findById(postLikeDto.getPost_id());
    Optional<PostVotes> postVotesOptional = postVotesRepository
        .findByPostIdAndUserId(postLikeDto.getPost_id(), currentUser.getId());
    if (post.isEmpty()
        || postVotesOptional.isPresent() && postVotesOptional.get().getValue() == -1) {
      object.put("result", false);
    }
    if (post.isPresent() && postVotesOptional.isEmpty()) {
      PostVotes postVotes = PostVotes.builder()
          .time(LocalDateTime.now())
          .value(-1)
          .post(post.get())
          .user(currentUser)
          .build();
      postVotesRepository.save(postVotes);
      object.put("result", true);
    }
    if (post.isPresent() && postVotesOptional.isPresent()
        && postVotesOptional.get().getValue() == 1) {
      PostVotes postVotes = postVotesRepository.getOne(postVotesOptional.get().getId());
      postVotes.setValue(-1);
      object.put("result", true);
    }
    return object;
  }

  private ObjectNode createObjectNode() {
    ObjectMapper mapper = new ObjectMapper();
    ObjectNode object = mapper.createObjectNode();
    return object;
  }
}
