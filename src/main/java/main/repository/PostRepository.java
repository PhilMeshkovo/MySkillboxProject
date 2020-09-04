package main.repository;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;
import main.model.Post;
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

  @Query(
      nativeQuery = true,
      value =
          "SELECT * FROM posts join post_comments on posts.id = post_comments.post_id where"
              + " is_active = 1 and moderation_status = 'ACCEPTED' AND posts.time < now()"
              + " group by posts.id order by count(post_comments.id) desc limit :limit offset :offset"
  )
  List<Post> findAllPostsSortedByComments(@Param("offset") Integer offset,
      @Param("limit") Integer limit);

  @Query(
      nativeQuery = true,
      value =
          "SELECT * FROM posts join post_votes on posts.id = post_votes.post_id "
              + "where  is_active = 1 and moderation_status = 'ACCEPTED' AND posts.time < now() "
              + "and post_votes.value = 1 group by posts.id order by count(post_votes.id) desc "
              + " limit :limit offset :offset"
  )
  List<Post> findAllPostsSortedByLikes(@Param("offset") Integer offset,
      @Param("limit") Integer limit);

  @Query(nativeQuery = true,
      value =
          "SELECT * FROM posts WHERE is_active = 1 AND "
              + "moderation_status = 'ACCEPTED' AND time < now() AND (title LIKE %:query%"
              + " OR text LIKE %:query%) limit :limit offset :offset")
  List<Post> findPostByQuery(@Param("offset") Integer offset,
      @Param("limit") Integer limit,
      @Param("query") String query);

  @Query(nativeQuery = true,
      value = "SELECT * FROM posts WHERE user_id = :id ")
  List<Post> findAllByAuthorId(@Param("id") Integer id);

  @Query(nativeQuery = true,
      value = "SELECT * FROM posts WHERE moderation_status = :status"
          + " AND is_active = 1 limit :limit offset :offset")
  List<Post> findAllPostsToModeration(@Param("offset") Integer offset,
      @Param("limit") Integer limit, @Param("status") String status);


  @Query(nativeQuery = true,
      value = "SELECT * FROM posts WHERE user_id = :id AND is_active = 1 AND"
          + " moderation_status = :status limit :limit offset :offset")
  List<Post> findAllMyPosts(@Param("offset") Integer offset, @Param("limit") Integer limit,
      @Param("status") String status, @Param("id") int id);


  @Query(nativeQuery = true,
      value = "SELECT * FROM posts WHERE is_active = 1 AND "
          + " moderation_status = 'ACCEPTED' AND time < now() AND "
          + " time >= :time AND time < :time + INTERVAL 1 DAY "
          + "limit :limit offset :offset")
  List<Post> findAllPostsByTime(@Param("offset") Integer offset,
      @Param("limit") Integer limit, @Param("time") Date time);

  @Query(nativeQuery = true,
      value = "SELECT min(time) FROM posts")
  LocalDateTime findFirstPublication();

  @Query(nativeQuery = true,
      value = "SELECT min(time) FROM posts WHERE user_id = :id AND is_active = 1 AND "
          + "moderation_status = 'ACCEPTED' AND time < now() ")
  LocalDateTime findFirstMyPublication(@Param("id") Integer id);

  @Query(nativeQuery = true,
      value = "SELECT * FROM posts WHERE id IN (:ids) AND is_active = 1 AND"
          + " moderation_status = 'ACCEPTED' AND time < now() limit :limit offset :offset")
  List<Post> findByIdIn(@Param("ids") List<Integer> ids, @Param("offset") Integer offset,
      @Param("limit") Integer limit);

  @Query(nativeQuery = true,
      value = "SELECT * FROM posts WHERE user_id = :id AND is_active = 0 limit :limit offset :offset")
  List<Post> findAllMyPostsInactive(@Param("offset") Integer offset, @Param("limit") Integer limit,
      @Param("id") int id);

}
