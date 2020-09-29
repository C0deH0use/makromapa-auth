package pl.code.house.makro.mapa.auth.domain.user;

import static org.springframework.http.HttpHeaders.AUTHORIZATION;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static org.springframework.http.ResponseEntity.ok;
import static pl.code.house.makro.mapa.auth.ApiConstraints.BASE_PATH;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@AllArgsConstructor
@RequestMapping(path = BASE_PATH + "/user", produces = APPLICATION_JSON_VALUE, consumes = APPLICATION_JSON_VALUE)
class UserOptOutResource {

  private final UserFacade facade;

  @DeleteMapping("/optout")
  ResponseEntity deleteMe(@AuthenticationPrincipal Authentication principal, @RequestHeader(AUTHORIZATION) String accessToken) {
    log.info("Registered request to remove user from: {}", principal.getName());

    facade.deleteUser(accessToken);
    return ok().build();
  }
}