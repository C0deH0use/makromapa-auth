package pl.code.house.makro.mapa.auth.domain.user;

import static java.util.function.Predicate.not;
import static java.util.stream.Collectors.toSet;
import static pl.code.house.makro.mapa.auth.domain.user.OAuth2Provider.FACEBOOK;
import static pl.code.house.makro.mapa.auth.domain.user.PremiumFeature.NON;
import static pl.code.house.makro.mapa.auth.domain.user.PremiumFeature.PREMIUM;

import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.social.facebook.api.User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pl.code.house.makro.mapa.auth.domain.user.dto.UserDto;
import pl.code.house.makro.mapa.auth.domain.user.dto.UserInfoDto;

@Slf4j
@Service
public class UserQueryFacade extends BaseUserFacade {

  private final TermsAndConditionsFacade termsAndConditionsFacade;

  public UserQueryFacade(UserRepository userRepository, TermsAndConditionsFacade termsAndConditionsFacade,
      UserAuthoritiesService authoritiesService) {
    super(userRepository, authoritiesService);
    this.termsAndConditionsFacade = termsAndConditionsFacade;
  }

  @Transactional(readOnly = true)
  public Optional<UserInfoDto> findUserById(UUID userId) {
    log.debug("Searching for User with id - `{}`", userId);

    return userRepository.findById(userId)
        .map(this::toUserInfo);
  }

  @Transactional(readOnly = true)
  public Optional<UserDto> findUserByToken(Jwt token) {
    String externalUserId = tryGetExternalUserId(token);
    OAuth2Provider oauth2Provider = tryGetOAuthProvider(token);
    log.debug("Searching for User authenticated by `{}` with externalId - `{}`", oauth2Provider, externalUserId);

    return userRepository.findByExternalIdAndAuthProvider(externalUserId, oauth2Provider)
        .map(this::checkTcAndReturnDto);
  }

  @Transactional(readOnly = true)
  public Optional<UserDto> findUserByProfile(User userProfile) {
    String externalUserId = userProfile.getId();
    log.debug("Searching for User authenticated by `{}` with externalId - `{}`", FACEBOOK, externalUserId);

    return userRepository.findByExternalIdAndAuthProvider(externalUserId, FACEBOOK)
        .map(this::checkTcAndReturnDto);
  }

  private UserDto checkTcAndReturnDto(BaseUser user) {
    if (getUserPremiumFeatures(user).contains(PREMIUM)) {
      termsAndConditionsFacade.isLatestTermsApproved(user.getTermsAndConditionsId());
    }
    return user.toDto();
  }

  private UserInfoDto toUserInfo(BaseUser user) {
    return user.toUserInfo(getUserPremiumFeatures(user));
  }

  private Set<PremiumFeature> getUserPremiumFeatures(BaseUser user) {
    return authoritiesService.getUserAuthorities(user.getId())
        .stream()
        .map(PremiumFeature::fromAuthority)
        .filter(not(NON::equals))
        .collect(toSet());
  }
}