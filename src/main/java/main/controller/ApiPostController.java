package main.controller;

import main.api.response.PostListApi;
import main.service.PostService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/post")
public class ApiPostController {

  @Autowired
  PostService postService;

  @GetMapping
  public PostListApi getAllPosts(
      @RequestParam(value = "offset", defaultValue = "0", required = false) Integer offset,
      @RequestParam(value = "itemPerPage", defaultValue = "10", required = false) Integer itemPerPage,
      @RequestParam(value = "mode", defaultValue = "recent", required = false) String mode) {
    return postService.getAllPosts(PageRequest.of(offset, itemPerPage), mode);
  }

  @GetMapping("/search")
  public PostListApi getAllPostsByTagAndTitle(
      @RequestParam(value = "offset", defaultValue = "0", required = false) Integer offset,
      @RequestParam(value = "itemPerPage", defaultValue = "10", required = false) Integer itemPerPage,
      @RequestParam(value = "query", defaultValue = "tagName", required = false) String query) {
    return postService.getAllPostsByTagAndTitle(offset, itemPerPage, query);
  }
}
