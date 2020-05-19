package main.model;

import lombok.Data;

@Data
public class RegisterForm {

  private String email;
  private String name;
  private String password;
  private String captcha;
  private String captcha_secret;
}
