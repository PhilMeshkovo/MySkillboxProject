package main.dto.request;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ChangePasswordRequest {

  private String code;
  private String password;
  private String captcha;
  private String captcha_secret;
}
