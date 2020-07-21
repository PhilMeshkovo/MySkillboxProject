package main.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class ResponsePostApi {

  private int id;
  private String time;
  private UserApi user;
  private String title;
  private String text;
  private int likeCount;
  private int dislikeCount;
  private int commentCount;
  private int viewCount;
}