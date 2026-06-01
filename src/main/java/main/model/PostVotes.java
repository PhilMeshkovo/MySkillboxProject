package main.model;

import java.time.LocalDateTime;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "post_votes")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PostVotes {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "id", nullable = false, updatable = false)
  private int id;

  @ManyToOne(cascade = CascadeType.ALL)
  private User user;

  @ManyToOne
  @JoinColumn(name = "post_id")
  private Post post;

  @Column(nullable = false)
  private LocalDateTime time;

  @Column(nullable = false)
  private int value;

}
