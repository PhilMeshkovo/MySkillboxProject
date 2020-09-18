package main.dto.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.web.multipart.MultipartFile;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PostProfileRequestWithPhoto {

  private MultipartFile photo;
  private String email;
  private String name;
  private String password;
  private Integer removePhoto;

  public PostProfileRequestWithPhoto(String email, String name) {
    this.email = email;
    this.name = name;
  }

  public PostProfileRequestWithPhoto(String email, String name, String password) {
    this.email = email;
    this.name = name;
    this.password = password;
  }
}
