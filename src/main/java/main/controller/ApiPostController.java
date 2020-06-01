package main.controller;

import main.api.response.PostByIdApi;
import main.api.response.PostListApi;
import main.service.PostService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/post")
public class ApiPostController {

  @Autowired
  PostService postService;

  @GetMapping
  public PostListApi getAllPosts(
      @RequestParam(value = "offset", defaultValue = "0", required = false) Integer offset,
      @RequestParam(value = "limit", defaultValue = "10", required = false) Integer limit,
      @RequestParam(value = "mode", defaultValue = "recent", required = false) String mode) {
    return postService.getAllPosts(PageRequest.of(offset, limit), mode);
  }

  @GetMapping("/search")
  public PostListApi getAllPostsByTagAndTitle(
      @RequestParam(value = "offset", defaultValue = "0", required = false) Integer offset,
      @RequestParam(value = "limit", defaultValue = "10", required = false) Integer limit,
      @RequestParam(value = "query", defaultValue = "tagName", required = false) String query) {
    return postService.getAllPostsByTagAndTitle(offset, limit, query);
  }

  @GetMapping("/{id}")
  @ResponseStatus(HttpStatus.OK)
  public PostByIdApi getPostById(@PathVariable int id) {
    return postService.findPostById(id);
  }

  @GetMapping("/byDate")
  public PostListApi getAllPostsByDate(
      @RequestParam(value = "offset", defaultValue = "0", required = false) Integer offset,
      @RequestParam(value = "limit", defaultValue = "10", required = false) Integer limit,
      @RequestParam(value = "date", defaultValue = "2007-01-01", required = false) String date) {
    return postService.getAllPostsByDate(offset, limit, date);
  }

  @GetMapping("/byTag")
  public PostListApi getAllPostsByTag(
      @RequestParam(value = "offset", defaultValue = "0", required = false) Integer offset,
      @RequestParam(value = "limit", defaultValue = "10", required = false) Integer limit,
      @RequestParam(value = "tag", defaultValue = "tagName", required = false) String tag) {
    return postService.getAllPostsByTag(offset, limit, tag);
  }

  @GetMapping("/moderation")
  public PostListApi getAllPostsToModeration(
      @RequestParam(value = "offset", defaultValue = "0", required = false) Integer offset,
      @RequestParam(value = "limit", defaultValue = "10", required = false) Integer limit,
      @RequestParam(value = "status", defaultValue = "new", required = false) String status) {
    return postService.getAllPostsToModeration(PageRequest.of(offset, limit), status);
  }
}
