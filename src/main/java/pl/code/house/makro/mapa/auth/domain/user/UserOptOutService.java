package pl.code.house.makro.mapa.auth.domain.user;

import static org.apache.commons.lang3.StringUtils.removeStartIgnoreCase;
import static org.apache.commons.lang3.StringUtils.trimToEmpty;

import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.oauth2.common.OAuth2AccessToken;
import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.security.oauth2.provider.token.TokenStore;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
class UserOptOutService {

  private final UserRepository userRepository;

  private final TokenStore tokenServices;

  void optoutUser(String authenticationToken) {
    String token = trimToEmpty(removeStartIgnoreCase(authenticationToken, "bearer"));
    OAuth2AccessToken accessToken = tokenServices.readAccessToken(token);
    OAuth2Authentication authentication = tokenServices.readAuthentication(token);

    log.info("Searching for user ['{}'] to be removed that is associated with access token.", authentication.getName());
    BaseUser user = userRepository.findById(UUID.fromString(authentication.getName()))
        .orElseThrow(() -> new IllegalArgumentException("Could not find user that would be assigned to the access token used with this request"));

    log.info("Revoking user ['{}'] access Token", user.getId());

    tokenServices.removeRefreshToken(accessToken.getRefreshToken());
    tokenServices.removeAccessToken(accessToken);
    log.info("User ['{}'] provided by {} is being removed from the system", user.getId(), user.getProvider());
    userRepository.deleteById(user.getId());
  }
}
