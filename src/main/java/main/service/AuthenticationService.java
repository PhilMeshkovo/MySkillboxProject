package main.service;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import lombok.Data;
import main.model.User;
import main.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.acls.model.NotFoundException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

@Data
@Service
public class AuthenticationService {

  private Map<String, Integer> authorizedUsers = new HashMap<>();

  @Autowired
  UserRepository userRepository;

  public Optional<User> getCurrentUser() {
    Authentication auth = SecurityContextHolder.getContext().getAuthentication();

    if (null == auth) {
      throw new NotFoundException("");
    }

    Object obj = auth.getPrincipal();
    String username = "";

    if (obj instanceof UserDetails) {
      username = ((UserDetails) obj).getUsername();
    } else {
      username = obj.toString();
    }

    Optional<User> optionalUser = userRepository.findByEmail(username);
    return optionalUser;
  }

}
