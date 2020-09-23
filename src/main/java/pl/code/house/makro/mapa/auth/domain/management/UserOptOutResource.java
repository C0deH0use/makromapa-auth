package pl.code.house.makro.mapa.auth.domain.management;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static org.springframework.http.ResponseEntity.ok;
import static pl.code.house.makro.mapa.auth.ApiConstraints.BASE_PATH;

import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import pl.code.house.makro.mapa.auth.domain.management.dto.UserDataResponseDto;
import pl.code.house.makro.mapa.auth.domain.user.UserFacade;

@Slf4j
@RestController
@AllArgsConstructor
@RequestMapping(path = BASE_PATH, produces = APPLICATION_JSON_VALUE, consumes = APPLICATION_JSON_VALUE)
class UserOptOutResource {

  private final UserFacade facade;

  @PostMapping(path = "/external/user/facebook/opt-out")
  ResponseEntity<UserDataResponseDto> userOptOut(Map<String, Object> request) {
    log.info("Registered Facebook User OptOut call. signedRequest: `{}`", request);
    //facade.deleteUser(accessToken);
    return ok(new UserDataResponseDto("https://www.makromapa.pl/", "200"));
  }

  @DeleteMapping("/user/optout")
  ResponseEntity deleteMe(@RequestHeader(HttpHeaders.AUTHORIZATION) String accessToken) {
    log.info("Registered request to remove user");

    facade.deleteUser(accessToken);
    return ok().build();
  }

}
