package main.dto.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PostCommentRequest {

  private Integer parent_id;
  private int post_id;
  private String text;

  public PostCommentRequest(int post_id, String text) {
    this.post_id = post_id;
    this.text = text;
  }
}
