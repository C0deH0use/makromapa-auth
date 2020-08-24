package pl.code.house.makro.mapa.auth.configuration;

import org.apache.commons.lang3.RandomStringUtils;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;

public class PasswordGenerator {

  @Test
  @DisplayName("generate password")
  void generatePassword() {
    //given
    String password = RandomStringUtils.randomAlphanumeric(10);
//    String password = "P@ssw0rd1";
    PasswordEncoder passwordEncoder = PasswordEncoderFactories.createDelegatingPasswordEncoder();

    //then
    System.out.println("Password : " + password);
    System.out.println("Password Encoded: " + passwordEncoder.encode(password));
  }

}
