package main.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class PostLikeRequest {

  @JsonProperty("post_id")
  private Integer postId;
}
