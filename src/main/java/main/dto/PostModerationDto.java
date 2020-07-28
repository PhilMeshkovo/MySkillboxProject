package main.dto;

import lombok.Data;

@Data
public class PostModerationDto {

  private Integer post_id;
  private String decision;
}
