package pl.code.house.makro.mapa.auth.domain.user;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static pl.code.house.makro.mapa.auth.ApiConstraints.AUTH_BASE_PATH;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import pl.code.house.makro.mapa.auth.domain.token.dto.AccessTokenDto;

@Slf4j
@RestController
@AllArgsConstructor
@RequestMapping(path = AUTH_BASE_PATH, produces = APPLICATION_JSON_VALUE, consumes = APPLICATION_JSON_VALUE)
class UserAuthorizationResource {

  private final UserTokenAuthenticationService userTokenAuthenticationService;

  @PostMapping(path = "/authorize")
  ResponseEntity authorizeToken(@AuthenticationPrincipal Jwt principal) {
    log.info("Authorizing user token ['{}']", principal.<String>getClaim("sub"));

    AccessTokenDto tokenDto = userTokenAuthenticationService.authorizePrincipal(principal);

    return ResponseEntity.ok(tokenDto);
  }
}
