package main.dto.request;

import lombok.Data;

@Data
public class RegisterFormRequest {

  private String e_mail;
  private String name;
  private String password;
  private String captcha;
  private String captcha_secret;
}
