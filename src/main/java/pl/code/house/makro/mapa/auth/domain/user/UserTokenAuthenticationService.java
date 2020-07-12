package pl.code.house.makro.mapa.auth.domain.user;

import static org.apache.commons.lang3.StringUtils.isBlank;
import static pl.code.house.makro.mapa.auth.domain.user.UserType.FREE_USER;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pl.code.house.makro.mapa.auth.domain.token.AccessTokenFacade;
import pl.code.house.makro.mapa.auth.domain.token.dto.AccessTokenDto;
import pl.code.house.makro.mapa.auth.error.InsufficientUserDetailsException;
import pl.code.house.makro.mapa.auth.error.NewTermsAndConditionsNotApprovedException;

@Slf4j
@Service
@AllArgsConstructor
public class UserTokenAuthenticationService {

  private final UserRepository repository;

  private final TermsAndConditionsRepository termsRepository;

  private final AccessTokenFacade accessTokenFacade;

  @Transactional
  public AccessTokenDto authorizePrincipal(Jwt jwtPrincipal) {
    String externalId = tryGetExternalUserId(jwtPrincipal);
    User user = repository.findByExternalId(externalId)
        .orElseGet(() -> createNewFreeUser(jwtPrincipal));

    if (UserType.PREMIUM_USER == user.getUserDetails().getType()) {
      TermsAndConditions latestTnC = termsRepository.findFirstByOrderByLastUpdatedDesc();
      boolean userNotApprovedLatestTnC = latestTnC.getId().equals(user.getTermsAndConditionsId());

      if (!userNotApprovedLatestTnC) {
        throw new NewTermsAndConditionsNotApprovedException("New terms and conditions are required for user approval");
      }
    }

    int revokedTokens = accessTokenFacade.revokeAllTokensFor(user.getId());
    if (revokedTokens > 0) {
      log.debug("Revoked {} token(s) for user `{}` before issuing new token", revokedTokens, user.getId());
    }
    return accessTokenFacade.issueTokenFor(user.getId());
  }

  private User createNewFreeUser(Jwt jwtPrincipal) {
    String externalId = tryGetExternalUserId(jwtPrincipal);
    AuthProvider authProvider = AuthProvider.fromIssuer(jwtPrincipal.getClaim("iss"));

    UserDetails userDetails = UserDetails.builder()
        .type(FREE_USER)
        .name(jwtPrincipal.getClaim("name"))
        .email(jwtPrincipal.getClaim("email"))
        .surname(jwtPrincipal.getClaim("family_name"))
        .picture(jwtPrincipal.getClaim("picture"))
        .build();
    User newUser = User.builder()
        .provider(authProvider)
        .externalId(externalId)
        .userDetails(userDetails)
        .build();

    log.info("Creating new FREE_USER `{}`. Authentication provider: {}", externalId, authProvider);
    return repository.save(newUser);
  }

  private String tryGetExternalUserId(Jwt principal) {
    String externalId = principal.getClaim("sub");
    if (isBlank(externalId)) {
      throw new InsufficientUserDetailsException("Authentication Token does not contain required data > external user Id is missing");
    }
    return externalId;
  }
}
