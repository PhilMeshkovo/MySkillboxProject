package main.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import javax.persistence.EntityNotFoundException;
import main.api.response.PostByIdApi;
import main.api.response.PostListApi;
import main.api.response.ResponsePostApi;
import main.api.response.ResponsePostApiToModeration;
import main.dto.ListTagsDto;
import main.dto.PostCommentDto;
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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PostService {

  private static final DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

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

  public PostListApi getAllPosts(Integer offset, Integer limit, String mode) {
    List<ResponsePostApi> responsePostApiList;
    if (mode.equalsIgnoreCase("RECENT")) {
      responsePostApiList = postRepository
          .findAllPostsOrderedByTimeDesc(offset, limit).stream()
          .map(p -> postMapper.postToResponsePostApi(p)).collect(Collectors.toList());
      List<ResponsePostApi> pageApiNew = commentMapper
          .addCommentsCountAndLikesForPosts(responsePostApiList);
      return new PostListApi(pageApiNew, pageApiNew.size());
    }
    if (mode.equalsIgnoreCase("EARLY")) {
      responsePostApiList = postRepository
          .findAllPostsOrderedByTime(offset, limit).stream()
          .map(p -> postMapper.postToResponsePostApi(p)).collect(Collectors.toList());
      List<ResponsePostApi> pageApiNew = commentMapper
          .addCommentsCountAndLikesForPosts(responsePostApiList);
      return new PostListApi(pageApiNew, pageApiNew.size());
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
      return new PostListApi(sortedPageApi, pageApiNew.size());
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
      return new PostListApi(sortedPageApi, pageApiNew.size());
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
    return new PostListApi(responsePostApis, responsePostApis.size());
  }

  public PostListApi getAllPostsByTag(Integer offset, Integer limit, String tag) {
    Optional<Tag> tagById = tagRepository.findTagByQuery(tag);
    if (!tagById.isEmpty()) {
      Set<Post> posts = tagById.get().getPosts();
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
    Page<Post> allPosts = postRepository.findAll(PageRequest.of(offset, limit));
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

      String[] arrayTags = tags.split(",");
      Set<Tag> setTags = Arrays.stream(arrayTags).map(t -> tagRepository.findTagByQuery(t).get())
          .collect(Collectors.toSet());
      DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
      LocalDate localDate = LocalDate.parse(time, dateFormatter);
      LocalDateTime localDateTime = localDate.atStartOfDay().plusHours(3L);
      if (localDateTime.isAfter(LocalDateTime.now().plusHours(3L))) {
        localDateTime = LocalDateTime.now().plusHours(3L);
      }
      Post post = Post.builder()
          .user(currentUser)
          .moderationStatus(ModerationStatus.NEW)
          .title(title)
          .isActive(active)
          .tags(setTags)
          .moderator(currentUser)
          .text(text)
          .time(localDateTime)
          .view_count(0)
          .build();
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

  @Transactional
  public JsonNode updatePost(int id, String time, Integer active, String title, String text,
      String tags) throws Exception {
    ObjectMapper mapper = new ObjectMapper();
    ObjectNode object = mapper.createObjectNode();
    User currentUser = userService.getCurrentUser();
    if (title.length() >= 10 && text.length() >= 500) {
      Optional<Post> postById = postRepository.findById(id);
      if (!postById.isEmpty() && postById.get().getUser().equals(currentUser)) {
        String[] arrayTags = tags.split(",");
        Set<Tag> setTags = Arrays.stream(arrayTags).map(t -> tagRepository.findTagByQuery(t).get())
            .collect(Collectors.toSet());
        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        LocalDate localDate = LocalDate.parse(time, dateFormatter);
        LocalDateTime localDateTime = localDate.atStartOfDay().plusHours(3L);
        if (localDateTime.isAfter(LocalDateTime.now().plusHours(3L))) {
          localDateTime = LocalDateTime.now().plusHours(3L);
        }
        Post postToUpdate = postRepository.getOne(id);
        postToUpdate.setTime(localDateTime);
        postToUpdate.setIsActive(active);
        postToUpdate.setTitle(title);
        postToUpdate.setText(text);
        postToUpdate.setTags(setTags);

        object.put("result", true);
      } else {
        throw new EntityNotFoundException("post does not exist or it is not yours");
      }
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

  public JsonNode addCommentToPost(PostCommentDto postCommentDto) throws Exception {
    ObjectMapper mapper = new ObjectMapper();
    ObjectNode object = mapper.createObjectNode();
    Integer postId = postCommentDto.getPostId();
    String text = postCommentDto.getText();
    Optional<Post> postById = postRepository.findById(postId);
    User currentUser = userService.getCurrentUser();
    if (!postById.isEmpty() && text.length() > 10 && postCommentDto.getParentId() != null
        && !postCommentRepository.findById(postCommentDto.getParentId()).isEmpty()
        && postCommentRepository.findById(postCommentDto.getParentId()).get().getPost()
        .equals(postById.get())) {
      PostComment parent = postCommentRepository.findById(postCommentDto.getParentId()).get();
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
    if (!postById.isEmpty() && text.length() > 9 && postCommentDto.getParentId() == null) {
      PostComment postComment = PostComment.builder()
          .post(postById.get())
          .user(currentUser)
          .time(LocalDateTime.now())
          .text(text)
          .build();
      PostComment savedPostComment = postCommentRepository.save(postComment);
      object.put("id", savedPostComment.getId());
    }
    if (postCommentDto.getParentId() != null && !postById.isEmpty() && !postCommentRepository
        .findById(postCommentDto.getParentId()).get().getPost().equals(postById.get())) {
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
          getWeightOfTag(tagByQuery.get());
          int amountTagInPosts = tagByQuery.get().getPosts().size();
          TagDto tagDto = TagDto.builder()
              .name(tagByQuery.get().getName())
              .weight(getWeightOfTag(tagByQuery.get()))
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
  public boolean moderationPost(Integer postId, String decision) throws Exception {
    Optional<Post> postById = postRepository.findById(postId);
    if (!postById.isEmpty() && decision.equalsIgnoreCase("accept")
        || !postById.isEmpty() && decision.equalsIgnoreCase("decline")) {
      Post postToModeration = postRepository.getOne(postId);
      if (decision.equalsIgnoreCase("accept")) {
        postToModeration.setModerationStatus(ModerationStatus.ACCEPTED);
      }
      if (decision.equalsIgnoreCase("decline")) {
        postToModeration.setModerationStatus(ModerationStatus.DECLINED);
      }
      User currentUser = userService.getCurrentUser();
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
  public JsonNode postLike(Integer postId) throws Exception {
    ObjectMapper mapper = new ObjectMapper();
    ObjectNode object = mapper.createObjectNode();
    User currentUser = userService.getCurrentUser();
    Optional<Post> post = postRepository.findById(postId);
    Optional<PostVotes> postVotesOptional = postVotesRepository
        .findByPostIdAndUserId(postId, currentUser.getId());
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
  public JsonNode postDislike(Integer postId) throws Exception {
    ObjectMapper mapper = new ObjectMapper();
    ObjectNode object = mapper.createObjectNode();
    User currentUser = userService.getCurrentUser();
    Optional<Post> post = postRepository.findById(postId);
    Optional<PostVotes> postVotesOptional = postVotesRepository
        .findByPostIdAndUserId(postId, currentUser.getId());
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
