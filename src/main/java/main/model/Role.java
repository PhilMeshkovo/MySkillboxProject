package main.model;

import lombok.NoArgsConstructor;
import org.springframework.security.core.GrantedAuthority;

@NoArgsConstructor
public class Role implements GrantedAuthority {

  private int id;
  private String nameRole;

  public Role(int id) {
    this.id = id;
  }

  public Role(int id, String nameRole) {
    this.id = id;
    this.nameRole = nameRole;
  }

  @Override
  public String getAuthority() {
    return nameRole;
  }
}
