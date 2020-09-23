package pl.code.house.makro.mapa.auth.domain.user;

import static org.apache.commons.lang3.StringUtils.removeStartIgnoreCase;
import static org.apache.commons.lang3.StringUtils.trimToEmpty;

import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.security.oauth2.provider.token.DefaultTokenServices;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
class UserOptOutService {

  private final UserRepository userRepository;

  private final DefaultTokenServices tokenServices;

  void optoutUser(String authenticationToken) {
    String accessToken = trimToEmpty(removeStartIgnoreCase(authenticationToken, "bearer"));
    OAuth2Authentication authenticatedUser = tokenServices.loadAuthentication(accessToken);

    log.info("Searching for user ['{}'] to be removed that is associated with access token.", authenticatedUser.getName());
    BaseUser user = userRepository.findById(UUID.fromString(authenticatedUser.getName()))
        .orElseThrow(() -> new IllegalArgumentException("Could not find user that would be assigned to the access token used with this request"));

    log.info("Revoking user ['{}'] access Token", user.getId());

    tokenServices.revokeToken(accessToken);
    log.info("User ['{}'] provided by {} is being removed from the system", user.getId(), user.getProvider());
    userRepository.deleteById(user.getId());
  }
}
