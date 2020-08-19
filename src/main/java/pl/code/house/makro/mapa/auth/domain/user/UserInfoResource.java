package pl.code.house.makro.mapa.auth.domain.user;

import static org.apache.commons.lang3.StringUtils.removeIgnoreCase;
import static org.springframework.http.HttpHeaders.AUTHORIZATION;
import static org.springframework.http.MediaType.ALL_VALUE;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static org.springframework.http.ResponseEntity.notFound;
import static pl.code.house.makro.mapa.auth.ApiConstraints.BASE_PATH;

import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.common.OAuth2AccessToken;
import org.springframework.security.oauth2.common.exceptions.InvalidTokenException;
import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.security.oauth2.provider.token.ResourceServerTokenServices;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import pl.code.house.makro.mapa.auth.domain.user.dto.UserInfoDto;

@Slf4j
@RestController
@AllArgsConstructor
@RequestMapping(path = BASE_PATH + "/user-info", produces = APPLICATION_JSON_VALUE, consumes = ALL_VALUE)
class UserInfoResource {

  private final UserRepository userRepository;

  private final ResourceServerTokenServices resourceServerTokenServices;

  @GetMapping
  ResponseEntity<UserInfoDto> userInfo(@AuthenticationPrincipal Authentication principal, @RequestHeader(AUTHORIZATION) String bearerToken) {
    log.debug("{} is request user info", principal.getName());

    String value = removeIgnoreCase(bearerToken, "Bearer ");
    OAuth2AccessToken token = resourceServerTokenServices.readAccessToken(value);
    if (token == null) {
      throw new InvalidTokenException("Token was not recognised");
    }

    if (token.isExpired()) {
      throw new InvalidTokenException("Token has expired");
    }

    OAuth2Authentication authentication = resourceServerTokenServices.loadAuthentication(token.getValue());

    return userRepository.findById(UUID.fromString(authentication.getName()))
        .map(BaseUser::toUserInfo)
        .map(ResponseEntity::ok)
        .orElse(notFound().build());
  }
}
