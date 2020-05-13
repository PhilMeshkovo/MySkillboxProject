package main.config;

import java.util.Collection;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;

public class UserAuthentication implements Authentication {

  private final UserDetailsImpl user;

  private boolean authenticated = true;

  public UserAuthentication(UserDetailsImpl userDetails) {
    this.user = userDetails;
  }

  @Override
  public Collection<? extends GrantedAuthority> getAuthorities() {
    return user.getAuthorities();
  }

  @Override
  public Object getCredentials() {
    return user.getPassword();
  }

  @Override
  public Object getDetails() {
    return user;
  }

  @Override
  public Object getPrincipal() {
    return user;
  }

  @Override
  public boolean isAuthenticated() {
    return authenticated;
  }

  @Override
  public void setAuthenticated(boolean isAuthenticated) throws IllegalArgumentException {
    this.authenticated = isAuthenticated;
  }

  @Override
  public String getName() {
    return user.getUsername();
  }
}
