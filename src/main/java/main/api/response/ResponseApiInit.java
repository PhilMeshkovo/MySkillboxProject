package main.api.response;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class ResponseApiInit {

  private String title;
  private String subtitle;
  private String phone;
  private String email;
  private String copyright;
  private String copyrightFrom;

}
