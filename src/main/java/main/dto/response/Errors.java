package main.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Errors {

  private String name;
  private String user;
  private String email;
  private String photo;
  private String title;
  private String text;
  private String image;
  private String code;
  private String password;
  private String captcha;
  private String parent;
  private String post;

}
