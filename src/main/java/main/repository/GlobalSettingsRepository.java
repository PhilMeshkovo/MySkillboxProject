package main.repository;

import java.util.Optional;
import main.model.GlobalSettings;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
public interface GlobalSettingsRepository extends JpaRepository<GlobalSettings, Integer> {

  @Modifying
  @Transactional
  @Query(nativeQuery = true,
      value = "UPDATE global_settings SET value = :value WHERE code = :code ")
  void updateValue(@Param("code") String code,
      @Param("value") String value);

  Optional<GlobalSettings> findByCode(String code);
}
