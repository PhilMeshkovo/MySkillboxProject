package main.repository;

import java.util.Optional;
import main.model.Tag;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface TagRepository extends JpaRepository<Tag, Integer> {

  @Query(nativeQuery = true,
      value = "SELECT * FROM tags WHERE name = :query ")
  Optional<Tag> findTagByQuery(@Param("query") String query);

}
