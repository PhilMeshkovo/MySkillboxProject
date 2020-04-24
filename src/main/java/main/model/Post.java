package main.model;

import java.util.Date;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import lombok.Data;
import lombok.NoArgsConstructor;
import main.model.enums.ModerationStatus;

@Entity
@Table(name = "posts")
@Data
@NoArgsConstructor
public class Post {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "id", nullable = false, updatable = false)
  private int id;

  @Column(name = "is_active", nullable = false)
  private int isActive;

  @Enumerated(EnumType.STRING)
  @Column(columnDefinition = "enum('NEW','ACCEPTED','DECLINED')")
  private ModerationStatus moderationStatus;

  @Column(name = "moderator_id")
  private int moderatorId;

  @ManyToOne(cascade = CascadeType.ALL)
  private User user;

  @Column(nullable = false)
  private Date time;

  @Column(nullable = false)
  private String title;

  @Column(nullable = false)
  private String text;

  @Column(nullable = false)
  private int value;
}
