package main.repository;

import main.model.Post;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface PostRepository extends JpaRepository<Post, Integer> {

  @Query(nativeQuery = true,
      value = "SELECT * FROM posts ORDER BY posts.time DESC ")
  Page<Post> findAllPostsOrderedByTime(Pageable pageable);

}
