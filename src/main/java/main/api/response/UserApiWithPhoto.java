package main.api.response;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class UserApiWithPhoto extends UserApi {

  private String photo;
}
