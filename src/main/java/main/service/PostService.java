package main.service;

import main.api.response.PostListApi;
import main.api.response.ResponsePostApi;
import main.mapper.PostMapper;
import main.repository.PostRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
public class PostService {

  @Autowired
  PostRepository postRepository;

  PostMapper postMapper = new PostMapper();

  public PostListApi getAllPosts(Pageable pageable) {
    Page<ResponsePostApi> pageApi = postRepository.findAll(pageable)
        .map(p -> postMapper.postToResponsePostApi(p));
    return new PostListApi(pageApi.toList(), pageApi.getTotalElements());
  }
}
