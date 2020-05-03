package main.repository;

import java.util.List;
import main.model.PostComment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface PostCommentRepository extends JpaRepository<PostComment, Integer> {

  @Query(nativeQuery = true,
      value = "SELECT * FROM post_comments WHERE post_id = :postId ")
  List<PostComment> findCommentsByPostId(@Param("postId") int postId);
}
