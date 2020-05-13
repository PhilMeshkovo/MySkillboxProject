package main.config;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UserDetailsImpl implements UserDetails {

  private List<Role> authorities;

  private String password;

  private String username;

  private boolean isAccountNonExpired;

  private boolean isAccountNonLocked;

  private boolean isCredentialsNonExpired;

  private boolean enabled;

}
