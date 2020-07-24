package main.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PostCommentDto {

  private Integer parent_id;
  private int post_id;
  private String text;

  public PostCommentDto(int post_id, String text) {
    this.post_id = post_id;
    this.text = text;
  }
}
