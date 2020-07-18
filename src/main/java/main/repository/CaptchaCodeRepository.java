package main.repository;

import java.time.LocalDateTime;
import java.util.Optional;
import main.model.CaptchaCode;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
public interface CaptchaCodeRepository extends JpaRepository<CaptchaCode, Integer> {

  Optional<CaptchaCode> findByCode(Integer captcha);

  @Modifying
  @Transactional
  void deleteByTimeBefore(LocalDateTime expiryDate);
}
