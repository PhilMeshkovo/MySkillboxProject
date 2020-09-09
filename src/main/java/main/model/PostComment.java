package main.model;

import java.time.LocalDateTime;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "post_comments")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PostComment {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "id", nullable = false, updatable = false)
  private int id;

  @ManyToOne(cascade = CascadeType.ALL)
  private PostComment parent;

  @ManyToOne(fetch = FetchType.EAGER)
  @JoinColumn(name = "post_id")
  private Post post;

  @ManyToOne(cascade = CascadeType.ALL)
  private User user;

  @Column(nullable = false)
  private LocalDateTime time;

  @Column(nullable = false)
  private String text;
}
