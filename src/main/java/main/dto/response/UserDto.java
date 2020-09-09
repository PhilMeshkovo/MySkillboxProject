package main.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserDto {

  private Integer id;
  private String name;
  private String photo;
  private String email;
  private boolean moderation;
  private Integer moderationCount;
  private boolean settings;
}
