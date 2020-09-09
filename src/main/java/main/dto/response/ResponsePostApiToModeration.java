package main.dto.response;

import java.time.LocalDateTime;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class ResponsePostApiToModeration {

  private int id;
  private LocalDateTime time;
  private UserResponse user;
  private String title;
  private String announce;

}
