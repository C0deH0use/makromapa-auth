package pl.code.house.makro.mapa.auth.domain.management;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static pl.code.house.makro.mapa.auth.ApiConstraints.EXTERNAL_AUTH_BASE_PATH;

import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import pl.code.house.makro.mapa.auth.domain.management.dto.UserDataResponseDto;

@Slf4j
@RestController
@AllArgsConstructor
@RequestMapping(path = EXTERNAL_AUTH_BASE_PATH + "/user", produces = APPLICATION_JSON_VALUE, consumes = APPLICATION_JSON_VALUE)
class UserOptOutResource {

  @PostMapping(path = "/facebook/opt-out")
  ResponseEntity<UserDataResponseDto> userOptOut(Map<String, Object> request) {
    log.info("Registered Facebook User OptOut call. signedRequest: `{}`", request);

    return ResponseEntity.ok(new UserDataResponseDto("https://www.makromapa.pl/", "200"));
  }

}
