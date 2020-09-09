package main.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.Transient;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import main.model.enums.ModerationStatus;

@Entity
@Table(name = "posts")
@Builder
@AllArgsConstructor
@NoArgsConstructor
//@EqualsAndHashCode(exclude = "posts")
public class Post {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "id", nullable = false, updatable = false)
  @Getter
  @Setter
  private int id;

  @Column(name = "is_active", nullable = false)
  @Getter
  @Setter
  private int isActive;

  @Enumerated(EnumType.STRING)
  @Column(columnDefinition = "enum('NEW','ACCEPTED','DECLINED')")
  @Getter
  @Setter
  private ModerationStatus moderationStatus;

  @JsonIgnore
  @OneToMany(fetch = FetchType.EAGER,mappedBy = "post")
  @Getter
  @Setter
  @Transient
  private Set<PostComment> postComments = new HashSet<>();

  @JsonIgnore
  @ManyToOne(cascade = CascadeType.ALL)
  @Getter
  @Setter
  private User moderator;

  @ManyToOne(cascade = CascadeType.ALL)
  @Getter
  @Setter
  private User user;

  @ManyToMany(fetch = FetchType.EAGER)
  @JoinTable(
      name = "tag2post",
      joinColumns = {@JoinColumn(name = "post_id", referencedColumnName = "id")},
      inverseJoinColumns = {@JoinColumn(name = "tag_id", referencedColumnName = "id")}
  )
  @Getter
  @Setter
  private Set<Tag> tags = new HashSet<>();

  @Column(nullable = false)
  @Getter
  @Setter
  private LocalDateTime time;

  @Column(nullable = false)
  @Getter
  @Setter
  private String title;

  @Column(nullable = false, columnDefinition = "Text")
  @Getter
  @Setter
  private String text;

  @Column(name = "view_count",nullable = false)
  @Getter
  @Setter
  private int viewCount;
}
