package main.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import java.util.HashSet;
import java.util.Set;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.Table;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "tags")
@NoArgsConstructor
@EqualsAndHashCode(exclude = "posts")
public class Tag {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "id", nullable = false, updatable = false)
  @Getter
  @Setter
  private int id;

  @Column(nullable = false)
  @Getter
  @Setter
  private String name;

  @JsonIgnore
  @ManyToMany(fetch = FetchType.EAGER)
  @JoinTable(
      name = "tag2post",
      joinColumns = {@JoinColumn(name = "tag_id", referencedColumnName = "id")},
      inverseJoinColumns = {@JoinColumn(name = "post_id", referencedColumnName = "id")}
  )
  @Getter
  @Setter
  private Set<Post> posts = new HashSet<>();
}
