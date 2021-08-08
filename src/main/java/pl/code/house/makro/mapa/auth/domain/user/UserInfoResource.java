package pl.code.house.makro.mapa.auth.domain.user;

import static java.util.UUID.fromString;
import static org.apache.commons.lang3.StringUtils.removeIgnoreCase;
import static org.springframework.http.HttpHeaders.AUTHORIZATION;
import static org.springframework.http.HttpStatus.OK;
import static org.springframework.http.MediaType.ALL_VALUE;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static pl.code.house.makro.mapa.auth.ApiConstraints.OAUTH_USER_PATH;
import static pl.code.house.makro.mapa.auth.error.UserOperationError.USER_NOT_FOUND;

import java.util.List;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.common.OAuth2AccessToken;
import org.springframework.security.oauth2.common.exceptions.InvalidTokenException;
import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.security.oauth2.provider.token.ResourceServerTokenServices;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import pl.code.house.makro.mapa.auth.domain.user.dto.UserInfoDto;
import pl.code.house.makro.mapa.auth.domain.user.dto.UserInfoUpdateDto;
import pl.code.house.makro.mapa.auth.error.UserNotExistsException;

@Slf4j
@Validated
@RestController
@AllArgsConstructor
@RequestMapping(path = OAUTH_USER_PATH, produces = APPLICATION_JSON_VALUE, consumes = ALL_VALUE)
class UserInfoResource {

  private static final String BEARER_PREFIX = "Bearer ";

  private final DefaultUserAvatars defaultAvatars;

  private final UserFacade facade;

  private final UserQueryFacade queryFacade;

  private final ResourceServerTokenServices resourceServerTokenServices;

  @ResponseStatus(OK)
  @GetMapping("/info")
  UserInfoDto userInfo(Authentication principal, @RequestHeader(AUTHORIZATION) String bearerToken) {
    UUID userId = extractUserId(bearerToken);
    log.debug("{} is requesting user info for {}", principal.getName(), userId);

    return queryFacade.findUserById(userId)
        .orElseThrow(() -> new UserNotExistsException(USER_NOT_FOUND, "User with following id `%s ` does not exists".formatted(userId)));
  }

  @ResponseStatus(OK)
  @PostMapping("/info")
  UserInfoDto updateUserInfo(Authentication principal, UserInfoUpdateDto updateDto) {
    UUID userId = fromString(principal.getName());
    log.debug("{} is updating user info for {}", principal.getName(), userId);

    return facade.updateUserInfo(userId, updateDto);
  }

  @ResponseStatus(OK)
  @GetMapping("/avatars")
  List<String> fetchPossibleAvatars() {
    log.debug("Fetching default avatars that can be used for user profile");

    return defaultAvatars.avatars();
  }

  private UUID extractUserId(String bearerToken) {
    String value = removeIgnoreCase(bearerToken, BEARER_PREFIX);
    OAuth2AccessToken token = resourceServerTokenServices.readAccessToken(value);
    if (token == null) {
      throw new InvalidTokenException("Token was not recognised");
    }

    if (token.isExpired()) {
      throw new InvalidTokenException("Token has expired");
    }

    OAuth2Authentication authentication = resourceServerTokenServices.loadAuthentication(token.getValue());
    return fromString(authentication.getName());
  }
}
