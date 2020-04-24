package main.model;

import java.util.Date;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "Posts")
@Data
@NoArgsConstructor
public class Post {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private int id;

  @Column(name = "is_active")
  private int isActive;

  @Enumerated(EnumType.STRING)
  @Column(columnDefinition = "enum",name = "moderation_status")
  private ModerationStatus moderationStatus;

  @Column(name = "moderator_id")
  private int moderatorId;

  @Column(name = "user_id")
  private int userId;

  private Date time;

  private String title;

  private String text;

  private int value;
}
