package main.api.response;

import java.util.Date;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class ResponsePostApiToModeration {

  private int id;
  private Date time;
  private UserApi user;
  private String title;
  private String announce;

}
