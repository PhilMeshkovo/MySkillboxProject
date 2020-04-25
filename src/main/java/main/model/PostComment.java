package main.model;

import java.util.Date;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "post_comments")
@Data
@NoArgsConstructor
public class PostComment {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "id", nullable = false, updatable = false)
  private int id;

  @ManyToOne(cascade = CascadeType.ALL)
  private PostComment parent;

  @ManyToOne(cascade = CascadeType.ALL)
  private Post post;

  @ManyToOne(cascade = CascadeType.ALL)
  private User user;

  @Column(nullable = false)
  private Date time;

  @Column(nullable = false)
  private String text;
}
