package main.dto.response;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CommentsApiResponse {

  private int id;
  private Long timestamp;
  private String text;
  private UserResponse user;
}
