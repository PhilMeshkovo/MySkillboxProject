package main.dto;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PostByIdApi extends ResponsePostApi {

  private List comments;
  private List<String> tags;
}
