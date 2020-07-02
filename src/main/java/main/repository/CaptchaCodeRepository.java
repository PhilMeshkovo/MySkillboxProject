package main.repository;

import java.util.Optional;
import main.model.CaptchaCode;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CaptchaCodeRepository extends JpaRepository<CaptchaCode,Integer> {

  Optional<CaptchaCode> findByCode(Integer captcha);
}
