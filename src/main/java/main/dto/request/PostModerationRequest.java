package main.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class PostModerationRequest {

  @JsonProperty("post_id")
  private Integer postId;
  private String decision;
}
