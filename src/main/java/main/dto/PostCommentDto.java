package main.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PostCommentDto {

  private Integer parentId;
  private int postId;
  private String text;

  public PostCommentDto(int postId, String text) {
    this.postId = postId;
    this.text = text;
  }
}
