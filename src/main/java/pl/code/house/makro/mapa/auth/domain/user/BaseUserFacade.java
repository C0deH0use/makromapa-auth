package pl.code.house.makro.mapa.auth.domain.user;

import static org.apache.commons.lang3.StringUtils.isBlank;
import static pl.code.house.makro.mapa.auth.domain.user.ExternalUser.newUserFrom;
import static pl.code.house.makro.mapa.auth.domain.user.OAuth2Provider.FACEBOOK;
import static pl.code.house.makro.mapa.auth.domain.user.OAuth2Provider.fromIssuer;
import static pl.code.house.makro.mapa.auth.domain.user.UserType.FREE_USER;
import static pl.code.house.makro.mapa.auth.error.UserOperationError.USER_NOT_FOUND;

import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.social.facebook.api.User;
import pl.code.house.makro.mapa.auth.error.InsufficientUserDetailsException;
import pl.code.house.makro.mapa.auth.error.UserNotExistsException;

@Slf4j
@RequiredArgsConstructor
abstract class BaseUserFacade {

  protected final UserRepository userRepository;
  protected final UserAuthoritiesService authoritiesService;

  protected ExternalUser createNewExternalUser(Jwt jwtPrincipal) {
    String externalId = tryGetExternalUserId(jwtPrincipal);
    OAuth2Provider oauth2Provider = fromIssuer(jwtPrincipal.getClaim("iss"));

    UserDetails userDetails = parseUserDetails(jwtPrincipal);

    return createNewFreeUser(externalId, oauth2Provider, userDetails);
  }

  protected ExternalUser createNewFacebookUser(User profile) {
    String externalId = profile.getId();

    UserDetails userDetails = parseUserDetails(profile);
    return createNewFreeUser(externalId, FACEBOOK, userDetails);
  }

  protected ExternalUser createNewFreeUser(String externalId, OAuth2Provider oauth2Provider, UserDetails userDetails) {
    log.info("Creating new FREE_USER `{}`. Authentication provider: {}", externalId, oauth2Provider);

    ExternalUser externalUser = userRepository.saveAndFlush(newUserFrom(oauth2Provider, userDetails, externalId));
    authoritiesService.insertUserAuthorities(externalUser.getId(), FREE_USER);
    return externalUser;
  }

  protected String tryGetExternalUserId(Jwt principal) {
    String externalId = principal.getClaim("sub");
    if (isBlank(externalId)) {
      throw new InsufficientUserDetailsException("Authentication Token does not contain required data > external user Id is missing");
    }

    return externalId;
  }

  protected OAuth2Provider tryGetOAuthProvider(Jwt principal) {
    String iss = principal.getClaim("iss");

    if (isBlank(iss)) {
      throw new InsufficientUserDetailsException("Authentication Token does not contain required data > Token Issuer is missing");
    }
    return fromIssuer(iss);
  }

  protected UserDetails parseUserDetails(Jwt jwtPrincipal) {
    return UserDetails.builder()
        .type(FREE_USER)
        .name(jwtPrincipal.getClaim("name"))
        .email(jwtPrincipal.getClaim("email"))
        .surname(jwtPrincipal.getClaim("family_name"))
        .build();
  }

  protected UserDetails parseUserDetails(User profile) {
    return UserDetails.builder()
        .type(FREE_USER)
        .name(profile.getFirstName())
        .email(profile.getEmail())
        .surname(profile.getLastName())
        .build();
  }

  protected Optional<BaseUser> findBasicUserByEmail(String email) {
    BaseUser user = userRepository.findUserWithPasswordByUserEmail(email)
        .orElseThrow(() -> new UserNotExistsException(USER_NOT_FOUND, "Could not find user by email: " + email));
    return Optional.of(user);
  }
}
