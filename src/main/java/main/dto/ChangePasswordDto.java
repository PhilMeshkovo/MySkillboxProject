package main.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ChangePasswordDto {

  private String code;
  private String password;
  private String captcha;
  private String captcha_secret;
}
