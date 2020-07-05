package main.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.web.multipart.MultipartFile;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class NewProfileForm {

  private String name;
  private String email;
  private String password;
  private String photo;
  private Integer removePhoto;

  public NewProfileForm(String name, String email) {
    this.name = name;
    this.email = email;
  }

  public NewProfileForm(String name, String email, String password) {
    this.name = name;
    this.email = email;
    this.password = password;
  }
  public NewProfileForm(String name, String email, String password, Integer removePhoto) {
    this.name = name;
    this.email = email;
    this.password = password;
    this.removePhoto = removePhoto;
  }

  public NewProfileForm(String name, String email, Integer removePhoto, String photo) {
    this.name = name;
    this.email = email;
    this.removePhoto = removePhoto;
    this.photo = photo;
  }

}
