package main.repository;

import java.util.HashSet;
import java.util.List;
import main.model.Post;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface PostRepository extends JpaRepository<Post, Integer> {

  @Query(nativeQuery = true,
      value =
          "SELECT * FROM posts WHERE is_active = 1 AND moderation_status = 'ACCEPTED' AND time < now()"
              + "  ORDER BY posts.time DESC limit :limit offset :offset")
  List<Post> findAllPostsOrderedByTimeDesc(@Param("offset") Integer offset,
      @Param("limit") Integer limit);

  @Query(nativeQuery = true,
      value =
          "SELECT * FROM posts WHERE is_active = 1 AND moderation_status = 'ACCEPTED' AND time < now()"
              + "  ORDER BY posts.time limit :limit offset :offset")
  List<Post> findAllPostsOrderedByTime(@Param("offset") Integer offset,
      @Param("limit") Integer limit);

  @Query(nativeQuery = true,
      value =
          "SELECT * FROM posts WHERE is_active = 1 AND moderation_status = 'ACCEPTED' AND time < now()"
              + "   limit :limit offset :offset")
  List<Post> findAllPostsPageable(@Param("offset") Integer offset,
      @Param("limit") Integer limit);

  @Query(nativeQuery = true,
      value = "SELECT * FROM posts WHERE title = :query ")
  HashSet<Post> findPostByQuery(@Param("query") String query);

  @Query(nativeQuery = true,
      value = "SELECT * FROM posts WHERE user_id = :id ")
  List<Post> findAllByAuthorId(@Param("id") Integer id);

  @Query(nativeQuery = true,
      value = "SELECT * FROM posts WHERE moderation_status = :status AND is_active = 1 ")
  Page<Post> findAllPostsToModeration(Pageable pageable, @Param("status") String status);


  @Query(nativeQuery = true,
      value = "SELECT * FROM posts WHERE user_id = :id ")
  Page<Post> findAllMyPosts(Pageable pageable, @Param("id") int id);


//  @Query(nativeQuery = true,
//      value =
//          "SELECT * FROM posts WHERE DATEPART(yy-mm-dd, time) LIKE ':date%' AND is_active = 1 AND moderation_status = 'ACCEPTED' "
//              + "AND time < now() limit :limit offset :offset")
//  List<Post> findAllByDate(@Param("offset")Integer offset,@Param("limit") Integer limit,
//      @Param("date") String date);
}
