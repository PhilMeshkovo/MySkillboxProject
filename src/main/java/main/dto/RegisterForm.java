package main.dto;

import lombok.Data;

@Data
public class RegisterForm {

  private String e_mail;
  private String name;
  private String password;
  private String captcha;
  private String captcha_secret;
}
