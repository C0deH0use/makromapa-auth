package pl.code.house.makro.mapa.auth.domain.user;

import static java.util.UUID.fromString;
import static org.apache.commons.lang3.StringUtils.removeIgnoreCase;
import static org.springframework.http.HttpHeaders.AUTHORIZATION;
import static org.springframework.http.MediaType.ALL_VALUE;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static org.springframework.http.ResponseEntity.notFound;
import static org.springframework.http.ResponseEntity.ok;
import static pl.code.house.makro.mapa.auth.ApiConstraints.BASE_PATH;

import java.util.List;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.common.OAuth2AccessToken;
import org.springframework.security.oauth2.common.exceptions.InvalidTokenException;
import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.security.oauth2.provider.token.ResourceServerTokenServices;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import pl.code.house.makro.mapa.auth.domain.user.dto.UserInfoDto;
import pl.code.house.makro.mapa.auth.domain.user.dto.UserInfoUpdateDto;

@Slf4j
@Validated
@RestController
@AllArgsConstructor
@RequestMapping(path = BASE_PATH + "/user-info", produces = APPLICATION_JSON_VALUE, consumes = ALL_VALUE)
class UserInfoResource {

  public static final String BEARER_PREFIX = "Bearer ";

  private final UserFacade facade;

  private final ResourceServerTokenServices resourceServerTokenServices;

  @GetMapping
  ResponseEntity<UserInfoDto> userInfo(@AuthenticationPrincipal Authentication principal, @RequestHeader(AUTHORIZATION) String bearerToken) {
    UUID userId = extractUserId(bearerToken);
    log.debug("{} is requesting user info for {}", principal.getName(), userId);

    return facade.findUserById(userId)
        .map(ResponseEntity::ok)
        .orElse(notFound().build());
  }

  @PostMapping
  ResponseEntity<UserInfoDto> updateUserInfo(@AuthenticationPrincipal Authentication principal, UserInfoUpdateDto updateDto) {
    UUID userId = fromString(principal.getName());
    log.debug("{} is updating user info for {}", principal.getName(), userId);

    UserInfoDto userInfoDto = facade.updateUserDetails(userId, parseUserDetails(updateDto));
    return ok(userInfoDto);
  }

  @GetMapping("/avatars")
  ResponseEntity<List<String>> fetchPossibleAvatars(@AuthenticationPrincipal Authentication principal,
      @Value("${user-profile.default.avatars}") List<String> defaultAvatars) {
    log.debug("Fetching default avatars that can be used for user profile");

    return ok(defaultAvatars);
  }

  private UserDetails parseUserDetails(UserInfoUpdateDto updateDto) {
    return UserDetails.builder()
        .name(updateDto.getName())
        .surname(updateDto.getSurname())
        .nickname(updateDto.getNickname())
        .picture(updateDto.getPicture())
        .build();
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
