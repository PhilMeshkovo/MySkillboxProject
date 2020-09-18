package main.dto.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PostProfileRequest {

  private String photo;
  private String email;
  private String name;
  private String password;
  private Integer removePhoto;

  public PostProfileRequest(String email, String name) {
    this.email = email;
    this.name = name;
  }

  public PostProfileRequest(String email, String name, String password) {
    this.email = email;
    this.name = name;
    this.password = password;
  }

  public PostProfileRequest(String photo, String email, String name, Integer removePhoto) {
    this.photo = photo;
    this.email = email;
    this.name = name;
    this.removePhoto = removePhoto;
  }
}
