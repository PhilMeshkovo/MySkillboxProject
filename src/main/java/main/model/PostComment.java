package main.model;

import java.util.Date;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "PostComments")
@Data
@NoArgsConstructor
public class PostComment {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private int id;

  @Column(name = "parent_id")
  private int parentId;

  @Column(name = "post_id")
  private int postId;

  @Column(name = "user_id")
  private int userId;

  private Date time;

  private String text;
}
