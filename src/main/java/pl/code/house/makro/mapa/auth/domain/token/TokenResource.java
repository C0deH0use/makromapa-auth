package pl.code.house.makro.mapa.auth.domain.token;

import static org.apache.commons.lang3.StringUtils.isBlank;
import static org.springframework.http.HttpStatus.UNAUTHORIZED;
import static org.springframework.http.MediaType.APPLICATION_FORM_URLENCODED_VALUE;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static pl.code.house.makro.mapa.auth.ApiConstraints.AUTH_BASE_PATH;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@AllArgsConstructor
@RequestMapping(path = AUTH_BASE_PATH, produces = APPLICATION_JSON_VALUE, consumes = APPLICATION_JSON_VALUE)
class TokenResource {

  private final AccessTokenFacade facade;

  @ResponseBody
  @PostMapping(path = "/token/introspect", consumes = APPLICATION_FORM_URLENCODED_VALUE)
  ResponseEntity introspectToken(@RequestParam(value = "token") String token) {
    log.info("Validating access token - {}", token);
    if (isBlank(token)) {
      return ResponseEntity.status(UNAUTHORIZED).build();
    }

    return facade.introspectAccessCode(token)
        .map(ResponseEntity::ok)
        .orElseGet(() -> ResponseEntity.status(UNAUTHORIZED).build());
  }
}
