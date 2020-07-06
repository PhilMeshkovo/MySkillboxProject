package main.repository;

import java.util.Optional;
import main.model.PostVotes;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface PostVotesRepository extends JpaRepository<PostVotes, Integer> {

  @Query(nativeQuery = true,
      value = "SELECT * FROM post_votes WHERE post_id = :post_id AND user_id = :user_id")
  Optional<PostVotes> findByPostIdAndUserId(@Param("post_id") Integer postId,
      @Param("user_id") int id);
}
