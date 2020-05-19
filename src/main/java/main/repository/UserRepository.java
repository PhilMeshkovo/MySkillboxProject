package main.repository;

import java.util.Optional;
import main.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository extends JpaRepository<User, Integer> {

  @Query(nativeQuery = true,
      value = "SELECT * FROM users WHERE email = :email ")
  Optional<User> findByEmail(@Param("email") String email);

  @Query(nativeQuery = true,
      value = "SELECT * FROM users WHERE name = :name ")
  User findByUsername(@Param("name") String name);
}
