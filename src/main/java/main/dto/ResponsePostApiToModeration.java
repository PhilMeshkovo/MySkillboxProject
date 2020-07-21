package main.dto;

import java.time.LocalDateTime;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class ResponsePostApiToModeration {

  private int id;
  private LocalDateTime time;
  private UserApi user;
  private String title;
  private String announce;

}
