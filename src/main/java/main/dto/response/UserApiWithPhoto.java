package main.dto.response;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class UserApiWithPhoto extends UserResponse {

  private String photo;
}
