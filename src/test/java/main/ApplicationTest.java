package main;

import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

public class ApplicationTest {

  @Test
  public void test() {
    System.out.println(new BCryptPasswordEncoder().encode("user"));
  }

}
