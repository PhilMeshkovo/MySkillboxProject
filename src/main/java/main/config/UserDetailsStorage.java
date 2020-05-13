package main.config;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import lombok.NonNull;
import org.springframework.stereotype.Component;

@Component
public class UserDetailsStorage {

  List<UserDetailsImpl> userDetailsList = new ArrayList<>();

  public Optional<UserDetailsImpl> findByUsername(@NonNull String username) {
    return userDetailsList.stream()
        .filter(p -> p.getUsername().equals(username)).findAny();
  }
}
