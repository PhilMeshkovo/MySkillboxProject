package main.model;

import java.time.LocalDateTime;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "CaptchaCodes")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CaptchaCode {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "id", nullable = false, updatable = false)
  private int id;


  @Column(nullable = false)
  private LocalDateTime time;

  @Column(nullable = false)
  private String code;

  @Column(name = "secret_code")
  private String secretCode;
}
