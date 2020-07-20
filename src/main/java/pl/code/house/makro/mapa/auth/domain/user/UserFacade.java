package pl.code.house.makro.mapa.auth.domain.user;

import static org.apache.commons.lang3.StringUtils.isBlank;
import static pl.code.house.makro.mapa.auth.domain.user.OAuth2Provider.fromIssuer;
import static pl.code.house.makro.mapa.auth.domain.user.User.newUserFrom;
import static pl.code.house.makro.mapa.auth.domain.user.UserType.FREE_USER;
import static pl.code.house.makro.mapa.auth.domain.user.UserType.PREMIUM_USER;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pl.code.house.makro.mapa.auth.domain.user.dto.UserDto;
import pl.code.house.makro.mapa.auth.error.InsufficientUserDetailsException;
import pl.code.house.makro.mapa.auth.error.NewTermsAndConditionsNotApprovedException;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserFacade {

  private final UserRepository userRepository;

  private final TermsAndConditionsRepository termsRepository;

  @Transactional
  public UserDto findUserByToken(Jwt token) {
    String externalUserId = tryGetExternalUserId(token);
    OAuth2Provider oauth2Provider = fromIssuer(token.getClaim("iss"));
    log.info("Searching for User authenticated by `{}` with externalId - `{}`", oauth2Provider, externalUserId);

    User user = userRepository.findByExternalIdAndAuthProvider(externalUserId)
        .orElseGet(() -> createNewFreeUser(token));

    if (PREMIUM_USER == user.getUserDetails().getType()) {
      TermsAndConditions latestTnC = termsRepository.findFirstByOrderByLastUpdatedDesc();
      boolean userNotApprovedLatestTnC = latestTnC.getId().equals(user.getTermsAndConditionsId());

      if (!userNotApprovedLatestTnC) {
        throw new NewTermsAndConditionsNotApprovedException("New terms and conditions are required for user approval");
      }
    }
    return user.toDto();
  }

  private User createNewFreeUser(Jwt jwtPrincipal) {
    String externalId = tryGetExternalUserId(jwtPrincipal);
    OAuth2Provider oauth2Provider = fromIssuer(jwtPrincipal.getClaim("iss"));

    UserDetails userDetails = UserDetails.builder()
        .type(FREE_USER)
        .name(jwtPrincipal.getClaim("name"))
        .email(jwtPrincipal.getClaim("email"))
        .surname(jwtPrincipal.getClaim("family_name"))
        .picture(jwtPrincipal.getClaim("picture"))
        .build();

    log.info("Creating new FREE_USER `{}`. Authentication provider: {}", externalId, oauth2Provider);

    return userRepository.saveAndFlush(newUserFrom(oauth2Provider, externalId, userDetails));
  }

  private String tryGetExternalUserId(Jwt principal) {
    String externalId = principal.getClaim("sub");
    if (isBlank(externalId)) {
      throw new InsufficientUserDetailsException("Authentication Token does not contain required data > external user Id is missing");
    }

    return externalId;
  }

}
