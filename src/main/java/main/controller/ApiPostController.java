package main.controller;

import com.fasterxml.jackson.databind.JsonNode;
import java.text.ParseException;
import javax.persistence.EntityNotFoundException;
import main.dto.request.AddPostRequest;
import main.dto.request.PostCommentRequest;
import main.dto.request.PostLikeRequest;
import main.dto.request.PostModerationRequest;
import main.dto.response.ListTagsResponse;
import main.dto.response.PostListResponse;
import main.service.PostService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class ApiPostController {

  @Autowired
  PostService postService;

  @GetMapping("/post")
  public PostListResponse getAllPosts(
      @RequestParam(value = "offset", defaultValue = "0", required = false) Integer offset,
      @RequestParam(value = "limit", defaultValue = "20", required = false) Integer limit,
      @RequestParam(value = "mode", defaultValue = "recent", required = false) String mode) {
    return postService.getAllPosts(offset, limit, mode);
  }

  @GetMapping("/post/search")
  public ResponseEntity<?> getAllPostsByTagAndTitle(
      @RequestParam(value = "offset", defaultValue = "0", required = false) Integer offset,
      @RequestParam(value = "limit", defaultValue = "10", required = false) Integer limit,
      @RequestParam(value = "query", defaultValue = "tagName", required = false) String query) {
    try {
      PostListResponse postListApi = postService.getAllPostsByTextAndTitle(offset, limit, query);
      return ResponseEntity.ok(postListApi);
    } catch (EntityNotFoundException e) {
      return new ResponseEntity<>(e.getMessage(), HttpStatus.NOT_FOUND);
    }

  }

  @GetMapping("/post/{id}")
  @ResponseStatus(HttpStatus.OK)
  public ResponseEntity<?> getPostById(@PathVariable int id) {
    try {
      return ResponseEntity.ok(postService.findPostById(id));
    } catch (EntityNotFoundException e) {
      return new ResponseEntity<>(e.getMessage(), HttpStatus.NOT_FOUND);
    }

  }

  @GetMapping("/post/byDate")
  public ResponseEntity<?> getAllPostsByDate(
      @RequestParam(value = "offset", defaultValue = "0", required = false) Integer offset,
      @RequestParam(value = "limit", defaultValue = "10", required = false) Integer limit,
      @RequestParam(value = "date", defaultValue = "2007-01-01", required = false) String date) {
    try {
      PostListResponse postListApi = postService.getAllPostsByDate(offset, limit, date);
      return ResponseEntity.ok(postListApi);
    } catch (EntityNotFoundException | ParseException e) {
      return new ResponseEntity<>(e.getMessage(), HttpStatus.NOT_FOUND);
    }

  }

  @GetMapping("/post/byTag")
  public ResponseEntity<?> getAllPostsByTag(
      @RequestParam(value = "offset", defaultValue = "0", required = false) Integer offset,
      @RequestParam(value = "limit", defaultValue = "10", required = false) Integer limit,
      @RequestParam(value = "tag", defaultValue = "tagName", required = false) String tag) {
    try {
      PostListResponse postListApi = postService.getAllPostsByTag(offset, limit, tag);
      return ResponseEntity.ok(postListApi);
    } catch (EntityNotFoundException e) {
      return new ResponseEntity<>(e.getMessage(), HttpStatus.NOT_FOUND);
    }
  }

  @GetMapping("/post/my")
  public PostListResponse getAllMyPosts(
      @RequestParam(value = "offset", defaultValue = "0", required = false) Integer offset,
      @RequestParam(value = "limit", defaultValue = "10", required = false) Integer limit,
      @RequestParam(value = "status", defaultValue = "inactive", required = false) String status) {
    return postService.getAllMyPosts(offset, limit, status);
  }

  @GetMapping("/post/moderation")
  public PostListResponse getAllPostsToModeration(
      @RequestParam(value = "offset", defaultValue = "0", required = false) Integer offset,
      @RequestParam(value = "limit", defaultValue = "10", required = false) Integer limit,
      @RequestParam(value = "status", defaultValue = "new", required = false) String status) {
    return postService.getAllPostsToModeration(offset, limit, status);
  }

  @PostMapping("/post")
  public JsonNode addPost(
      @RequestBody AddPostRequest addPostDto) {
    JsonNode object = postService.addPost(addPostDto);
    return object;
  }

  @PutMapping("/post/{id}")
  public ResponseEntity<?> updatePost(
      @PathVariable int id,
      @RequestBody AddPostRequest addPostDto) {
    try {
      return ResponseEntity.ok(postService.updatePost(id, addPostDto));
    } catch (EntityNotFoundException e) {
      return new ResponseEntity<>(e.getMessage(), HttpStatus.NOT_FOUND);
    } catch (Exception e) {
      return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
    }
  }

  @PostMapping("/comment")
  public ResponseEntity<?> addComment(@RequestBody PostCommentRequest postCommentDto) {
    JsonNode jsonNode = jsonNode = postService.addCommentToPost(postCommentDto);
    if (jsonNode.has("error")) {
      return new ResponseEntity<>(jsonNode, HttpStatus.BAD_REQUEST);
    }
    return ResponseEntity.ok(jsonNode);
  }

  @GetMapping("/tag")
  public ResponseEntity<?> getTags(@RequestParam(value = "query", defaultValue = "") String query) {

    try {
      ListTagsResponse listTagsDto = postService.getTag(query);
      return ResponseEntity.ok(listTagsDto);
    } catch (EntityNotFoundException e) {
      return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
    }
  }

  @PostMapping("/moderation")
  public ResponseEntity<?> moderationPost(
      @RequestBody PostModerationRequest postModerationDto) {
    try {
      return ResponseEntity.ok(postService.moderationPost(postModerationDto));
    } catch (Exception e) {
      return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
    }
  }

  @GetMapping("/calendar")
  public ResponseEntity<?> getAllPostsInYear(
      @RequestParam(value = "year", defaultValue = "") String year) {
    JsonNode object = postService.getAllPostsInYear(year);
    return new ResponseEntity<>(object, HttpStatus.OK);
  }

  @PostMapping("/post/like")
  public ResponseEntity<?> postLike(
      @RequestBody PostLikeRequest postLikeDto) {
    try {
      return ResponseEntity.ok(postService.postLike(postLikeDto));
    } catch (EntityNotFoundException e) {
      return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
    }
  }

  @PostMapping("/post/dislike")
  public ResponseEntity<?> postDislike(
      @RequestBody PostLikeRequest postLikeDto) {
    try {
      return ResponseEntity.ok(postService.postDislike(postLikeDto));
    } catch (EntityNotFoundException e) {
      return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
    }
  }
}
