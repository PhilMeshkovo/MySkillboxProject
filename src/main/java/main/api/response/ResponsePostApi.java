package main.api.response;

import java.time.LocalDateTime;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class ResponsePostApi {

  private int id;
  private LocalDateTime time;
  private UserApi user;
  private String title;
  private String announce;
  private int likeCount;
  private int dislikeCount;
  private int commentCount;
  private int viewCount;
}