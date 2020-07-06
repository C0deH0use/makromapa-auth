package pl.code.house.makro.mapa.auth.domain.user;

import static pl.code.house.makro.mapa.auth.domain.user.AuthProvider.GOOGLE;

import java.util.UUID;
import java.util.function.Supplier;
import lombok.AllArgsConstructor;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pl.code.house.makro.mapa.auth.domain.user.dto.AccessCode;

@Service
@AllArgsConstructor
public class UserConversion {

  private final UserRepository repository;

  @Transactional
  public AccessCode convertTokenToCode(Jwt jwtPrincipal) {
    repository.findByExternalId(jwtPrincipal.getClaim("sub"))
        .orElseGet(() -> createNewUserFrom(jwtPrincipal));

    return new AccessCode("sss");
  }

  private User createNewUserFrom(Jwt jwtPrincipal) {
    UserDetails userDetails = UserDetails.builder()
        .name(jwtPrincipal.getClaim("name"))
        .email(jwtPrincipal.getClaim("email"))
        .surname(jwtPrincipal.getClaim("family_name"))
        .picture(jwtPrincipal.getClaim("picture"))
        .build();
    User newUser = User.builder()
        .provider(GOOGLE)
        .externalId(jwtPrincipal.getClaim("sub"))
        .userDetails(userDetails)
        .build();
    return repository.save(newUser);
  }
}
