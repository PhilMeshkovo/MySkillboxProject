package main.dto.response;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PostByIdResponse extends ResponsePostApi {

  private List comments;
  private List<String> tags;
}
