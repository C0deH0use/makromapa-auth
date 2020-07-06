package pl.code.house.makro.mapa.auth.api.google;


import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static pl.code.house.makro.mapa.auth.api.ApiConstraints.GOOGLE_AUTH_BASE_PATH;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import pl.code.house.makro.mapa.auth.domain.user.UserConversion;
import pl.code.house.makro.mapa.auth.domain.user.dto.AccessCode;

@Slf4j
@RestController
@AllArgsConstructor
@RequestMapping(path = GOOGLE_AUTH_BASE_PATH, produces = APPLICATION_JSON_VALUE, consumes = APPLICATION_JSON_VALUE)
class GoogleAuthorizationResource {

  private UserConversion userConversion;

  @PostMapping(path = "/authorize")
  ResponseEntity authorizeToken(@AuthenticationPrincipal Jwt principal) {
    log.info("Authorizing new access token ['{}']", principal.<String>getClaim("sub"));

    AccessCode accessCode = userConversion.convertTokenToCode(principal);

    return ResponseEntity.ok(accessCode);
  }
}
