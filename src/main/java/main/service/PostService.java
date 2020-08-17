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
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import javax.persistence.EntityNotFoundException;
import javax.servlet.http.HttpServletRequest;
import main.dto.AddPostDto;
import main.dto.ListTagsDto;
import main.dto.PostByIdApi;
import main.dto.PostCommentDto;
import main.dto.PostLikeDto;
import main.dto.PostListApi;
import main.dto.PostModerationDto;
import main.dto.ResponsePostApi;
import main.dto.ResponsePostApiWithAnnounce;
import main.dto.TagDto;
import main.mapper.CommentMapper;
import main.mapper.PostMapper;
import main.model.Post;
import main.model.PostComment;
import main.model.PostVotes;
import main.model.Tag;
import main.model.User;
import main.model.enums.ModerationStatus;
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

  private static final DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

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
  UserService userService;

  @Autowired
  AuthenticationService authenticationService;

  private static Map<String, Integer> authorizedUsers = UserService.getAuthorizedUsers();

  public PostListApi getAllPosts(Integer offset, Integer limit, String mode) {
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
      return new PostListApi(responseWithAnnounceList, pageApiNew.size());
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
      return new PostListApi(responseWithAnnounceList, pageApiNew.size());
    }
    if (mode.equalsIgnoreCase("POPULAR")) {
      responsePostApiList = postRepository
          .findAllPostsPageable(offset, limit).stream()
          .map(p -> postMapper.postToResponsePostApi(p)).collect(Collectors.toList());
      List<ResponsePostApi> pageApiNew = commentMapper
          .addCommentsCountAndLikesForPosts(responsePostApiList);
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
      List<ResponsePostApiWithAnnounce> responseWithAnnounceList =
          sortedPageApi.stream().map(p -> postMapper.responsePostApiToResponseWithAnnounce(p)).
              collect(Collectors.toList());
      return new PostListApi(responseWithAnnounceList, pageApiNew.size());
    }
    if (mode.equalsIgnoreCase("BEST")) {
      responsePostApiList = postRepository
          .findAllPostsPageable(offset, limit).stream()
          .map(p -> postMapper.postToResponsePostApi(p)).collect(Collectors.toList());
      List<ResponsePostApi> pageApiNew = commentMapper
          .addCommentsCountAndLikesForPosts(responsePostApiList);
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
      List<ResponsePostApiWithAnnounce> responseWithAnnounceList =
          sortedPageApi.stream().map(p -> postMapper.responsePostApiToResponseWithAnnounce(p)).
              collect(Collectors.toList());
      return new PostListApi(responseWithAnnounceList, pageApiNew.size());
    }
    return null;
  }

  public PostListApi getAllPostsByTextAndTitle(Integer offset, Integer limit, String query)
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
    return new PostListApi(responseWithAnnounceList, responsePostApis.size());
  }

  public PostListApi getAllPostsByTag(Integer offset, Integer limit, String tag) {
    Optional<Tag> tagById = tagRepository.findTagByQuery(tag);
    if (!tagById.isEmpty()) {
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
        return new PostListApi(responseWithAnnounceList, responsePostApis.size());
      } else {
        throw new EntityNotFoundException("No active posts or moderated");
      }
    } else {
      throw new EntityNotFoundException("Nothing found");
    }
  }

  public PostListApi getAllPostsByDate(Integer offset, Integer limit, String date)
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
      return new PostListApi(responseWithAnnounceList, responsePostApis.size());
    } else {
      throw new EntityNotFoundException("Nothing found");
    }
  }

  @Transactional
  public PostByIdApi findPostById(int postId) {
    Optional<Post> optional = postRepository.findById(postId);
    String sessionId = request.getSession().getId();
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

        if (!authorizedUsers.containsKey(sessionId) || authorizedUsers.containsKey(sessionId)
            && post.getUser().getId() != authorizedUsers.get(sessionId) &&
            userRepository.findById(authorizedUsers.get(sessionId)).get().getIsModerator() != 1) {
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

  public PostListApi getAllMyPosts(Integer offset, Integer limit, String status) throws Exception {
    User currentUser = authenticationService.getCurrentUser();
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
      return new PostListApi(responseWithAnnounceList, listResponseApi.size());
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
      return new PostListApi(responseWithAnnounceList, listResponseApi.size());
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
      return new PostListApi(responseWithAnnounceList, listResponseApi.size());
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
      return new PostListApi(responseWithAnnounceList, listResponseApi.size());
    }
    return null;
  }

  public PostListApi getAllPostsToModeration(Integer offset, Integer limit, String status) {
    List<ResponsePostApi> postApiNew = postRepository
        .findAllPostsToModeration(offset, limit, status).stream()
        .map(p -> postMapper.postToResponsePostApi(p)).collect(Collectors.toList());
    List<ResponsePostApi> responsePosts = commentMapper
        .addCommentsCountAndLikesForPosts(postApiNew);
    List<ResponsePostApiWithAnnounce> responseWithAnnounceList =
        responsePosts.stream().map(p -> postMapper.responsePostApiToResponseWithAnnounce(p)).
            collect(Collectors.toList());
    return new PostListApi(responseWithAnnounceList, responsePosts.size());
  }

  public JsonNode addPost(AddPostDto addPostDto)
      throws Exception {
    ObjectMapper mapper = new ObjectMapper();
    ObjectNode object = mapper.createObjectNode();
    if (addPostDto.getTitle().length() >= 10 && addPostDto.getText().length() >= 500) {
      User currentUser = authenticationService.getCurrentUser();

      Set<Tag> setTags = Arrays.stream(addPostDto.getTags())
          .map(t -> tagRepository.findTagByQuery(t).get())
          .collect(Collectors.toSet());

      LocalDateTime localDateTime = LocalDateTime
          .ofInstant(Instant.ofEpochSecond(addPostDto.getTimestamp()),
              ZoneId.systemDefault());
      if (localDateTime.isAfter(LocalDateTime.now().plusHours(3L))) {
        localDateTime = LocalDateTime.now().plusHours(3L);
      }
      Post post = Post.builder()
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
  public JsonNode updatePost(int id, AddPostDto addPostDto) throws Exception {
    ObjectMapper mapper = new ObjectMapper();
    ObjectNode object = mapper.createObjectNode();
    User currentUser = authenticationService.getCurrentUser();
    if (addPostDto.getTitle().length() >= 10 && addPostDto.getText().length() >= 500) {
      Optional<Post> postById = postRepository.findById(id);
      if (!postById.isEmpty() && postById.get().getUser().equals(currentUser)) {
        Set<Tag> setTags = Arrays.stream(addPostDto.getTags())
            .map(t -> tagRepository.findTagByQuery(t).get())
            .collect(Collectors.toSet());
        LocalDateTime localDateTime = LocalDateTime
            .ofInstant(Instant.ofEpochSecond(addPostDto.getTimestamp()),
                ZoneId.systemDefault());
        if (localDateTime.isAfter(LocalDateTime.now().plusHours(3L))) {
          localDateTime = LocalDateTime.now().plusHours(3L);
        }
        Post postToUpdate = postRepository.getOne(id);
        postToUpdate.setTime(localDateTime);
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

  public JsonNode addCommentToPost(PostCommentDto postCommentDto) throws Exception {
    ObjectMapper mapper = new ObjectMapper();
    ObjectNode object = mapper.createObjectNode();
    Integer postId = postCommentDto.getPost_id();
    String text = postCommentDto.getText();
    Optional<Post> postById = postRepository.findById(postId);
    User currentUser = authenticationService.getCurrentUser();
    if (!postById.isEmpty() && text.length() > 10 && postCommentDto.getParent_id() != null
        && !postCommentRepository.findById(postCommentDto.getParent_id()).isEmpty()
        && postCommentRepository.findById(postCommentDto.getParent_id()).get().getPost()
        .equals(postById.get())) {
      PostComment parent = postCommentRepository.findById(postCommentDto.getParent_id()).get();
      PostComment postComment = PostComment.builder()
          .post(postById.get())
          .parent(parent)
          .user(currentUser)
          .time(LocalDateTime.now())
          .text(text)
          .build();
      PostComment savedPostComment = postCommentRepository.save(postComment);
      object.put("id", savedPostComment.getId());
    }
    if (!postById.isEmpty() && text.length() > 9 && postCommentDto.getParent_id() == null) {
      PostComment postComment = PostComment.builder()
          .post(postById.get())
          .user(currentUser)
          .time(LocalDateTime.now())
          .text(text)
          .build();
      PostComment savedPostComment = postCommentRepository.save(postComment);
      object.put("id", savedPostComment.getId());
    }
    if (postCommentDto.getParent_id() != null && !postById.isEmpty() && !postCommentRepository
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
    if (!postById.isEmpty() && text.length() < 10) {
      object.put("result", false);
      ObjectNode objectError = mapper.createObjectNode();
      objectError.put("text", "Comment text too short");
      object.put("error", objectError);
    }

    return object;
  }

  public ListTagsDto getTag(String query) {
    ListTagsDto listTagsDto = new ListTagsDto();
    long totalPosts = postRepository.count();
    List<TagDto> tagDtoList = new ArrayList<>();
    if (query.equals("")) {
      List<Tag> allTags = tagRepository.findAll();
      for (Tag tag : allTags) {
        TagDto tagDto = TagDto.builder()
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
        if (!tagByQuery.isEmpty()) {
          double weight = getWeightOfTag(tagByQuery.get());
          TagDto tagDto = TagDto.builder()
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
  public boolean moderationPost(PostModerationDto postModerationDto) throws Exception {
    Optional<Post> postById = postRepository.findById(postModerationDto.getPost_id());
    if (!postById.isEmpty() && postModerationDto.getDecision().equalsIgnoreCase("accept")
        || !postById.isEmpty() && postModerationDto.getDecision().equalsIgnoreCase("decline")) {
      Post postToModeration = postRepository.getOne(postModerationDto.getPost_id());
      if (postModerationDto.getDecision().equalsIgnoreCase("accept")) {
        postToModeration.setModerationStatus(ModerationStatus.ACCEPTED);
      }
      if (postModerationDto.getDecision().equalsIgnoreCase("decline")) {
        postToModeration.setModerationStatus(ModerationStatus.DECLINED);
      }
      User currentUser = authenticationService.getCurrentUser();
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
  public JsonNode postLike(PostLikeDto postLikeDto) throws Exception {
    ObjectMapper mapper = new ObjectMapper();
    ObjectNode object = mapper.createObjectNode();
    User currentUser = authenticationService.getCurrentUser();
    Optional<Post> post = postRepository.findById(postLikeDto.getPost_id());
    Optional<PostVotes> postVotesOptional = postVotesRepository
        .findByPostIdAndUserId(postLikeDto.getPost_id(), currentUser.getId());
    if (post.isEmpty() || !postVotesOptional.isEmpty() && postVotesOptional.get().getValue() == 1) {
      object.put("result", false);
    }
    if (!post.isEmpty() && postVotesOptional.isEmpty()) {
      PostVotes postVotes = PostVotes.builder()
          .time(LocalDateTime.now())
          .value(1)
          .post(post.get())
          .user(currentUser)
          .build();
      postVotesRepository.save(postVotes);
      object.put("result", true);
    }
    if (!post.isEmpty() && !postVotesOptional.isEmpty()
        && postVotesOptional.get().getValue() == -1) {
      PostVotes postVotes = postVotesRepository.getOne(postVotesOptional.get().getId());
      postVotes.setValue(1);
      object.put("result", true);
    }
    return object;
  }

  @Transactional
  public JsonNode postDislike(PostLikeDto postLikeDto) throws Exception {
    ObjectMapper mapper = new ObjectMapper();
    ObjectNode object = mapper.createObjectNode();
    User currentUser = authenticationService.getCurrentUser();
    Optional<Post> post = postRepository.findById(postLikeDto.getPost_id());
    Optional<PostVotes> postVotesOptional = postVotesRepository
        .findByPostIdAndUserId(postLikeDto.getPost_id(), currentUser.getId());
    if (post.isEmpty()
        || !postVotesOptional.isEmpty() && postVotesOptional.get().getValue() == -1) {
      object.put("result", false);
    }
    if (!post.isEmpty() && postVotesOptional.isEmpty()) {
      PostVotes postVotes = PostVotes.builder()
          .time(LocalDateTime.now())
          .value(-1)
          .post(post.get())
          .user(currentUser)
          .build();
      postVotesRepository.save(postVotes);
      object.put("result", true);
    }
    if (!post.isEmpty() && !postVotesOptional.isEmpty()
        && postVotesOptional.get().getValue() == 1) {
      PostVotes postVotes = postVotesRepository.getOne(postVotesOptional.get().getId());
      postVotes.setValue(-1);
      object.put("result", true);
    }
    return object;
  }
}
