package main.api.response;


import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CommentsApi {

  private int id;

  private LocalDateTime time;

  private String text;

  private UserApi user;
}
