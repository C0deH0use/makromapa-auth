package pl.code.house.makro.mapa.auth.domain.user;

import static java.util.function.Predicate.not;
import static java.util.stream.Collectors.toSet;
import static org.springframework.util.Assert.hasText;
import static pl.code.house.makro.mapa.auth.domain.user.CodeType.REGISTRATION;
import static pl.code.house.makro.mapa.auth.domain.user.CodeType.RESET_PASSWORD;
import static pl.code.house.makro.mapa.auth.domain.user.OAuth2Provider.FACEBOOK;
import static pl.code.house.makro.mapa.auth.domain.user.PremiumFeature.NON;
import static pl.code.house.makro.mapa.auth.domain.user.UserType.DRAFT_USER;
import static pl.code.house.makro.mapa.auth.domain.user.UserType.FREE_USER;
import static pl.code.house.makro.mapa.auth.domain.user.UserWithPassword.newDraftFrom;
import static pl.code.house.makro.mapa.auth.error.UserOperationError.DRAFT_NOT_FOUND;
import static pl.code.house.makro.mapa.auth.error.UserOperationError.USER_NOT_FOUND;
import static pl.code.house.makro.mapa.auth.error.UserOperationError.VALIDATION_CODE_NOT_VALID;

import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.social.facebook.api.User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pl.code.house.makro.mapa.auth.domain.user.dto.ActivateUserRequest;
import pl.code.house.makro.mapa.auth.domain.user.dto.CommunicationDto;
import pl.code.house.makro.mapa.auth.domain.user.dto.NewUserRequest;
import pl.code.house.makro.mapa.auth.domain.user.dto.UserDto;
import pl.code.house.makro.mapa.auth.domain.user.dto.UserInfoDto;
import pl.code.house.makro.mapa.auth.domain.user.dto.UserInfoUpdateDto;
import pl.code.house.makro.mapa.auth.domain.user.dto.VerificationCodeDto;
import pl.code.house.makro.mapa.auth.error.PasswordResetException;
import pl.code.house.makro.mapa.auth.error.UserAlreadyExistsException;
import pl.code.house.makro.mapa.auth.error.UserNotExistsException;
import pl.code.house.makro.mapa.auth.error.UserRegistrationException;

@Slf4j
@Service
public class UserFacade extends BaseUserFacade {

  private final PasswordEncoder passwordEncoder;
  private final UserOptOutService optOutService;
  private final VerificationCodeService verificationCodeService;

  public UserFacade(UserRepository userRepository, UserAuthoritiesService authoritiesService, PasswordEncoder passwordEncoder,
      UserOptOutService optOutService,
      VerificationCodeService verificationCodeService) {
    super(userRepository, authoritiesService);
    this.passwordEncoder = passwordEncoder;
    this.optOutService = optOutService;
    this.verificationCodeService = verificationCodeService;
  }

  public static String maskEmail(String email) {
    return email.replaceAll("(^[^@]{3}|(?!^)\\G)[^@]", "$1*");
  }

  @Transactional
  public UserDto createUser(User profile) {
    checkIfUserExists(profile.getId(), FACEBOOK);

    return createNewFacebookUser(profile).toDto();
  }

  @Transactional
  public UserDto createUser(Jwt jwtPrincipal) {
    String externalUserId = tryGetExternalUserId(jwtPrincipal);
    OAuth2Provider oauth2Provider = tryGetOAuthProvider(jwtPrincipal);
    checkIfUserExists(externalUserId, oauth2Provider);

    return createNewExternalUser(jwtPrincipal).toDto();
  }

  @Transactional
  public CommunicationDto registerNewUser(NewUserRequest newUserRequest) {
    log.info("Registering new DRAFT User `{}` as BASIC_AUTH authentication provider", maskEmail(newUserRequest.getEmail()));

    Optional<BaseUser> userByEmail = userRepository.findUserWithPasswordByUserEmail(newUserRequest.getEmail());
    if (userByEmail.isPresent()) {
      if (userByEmail.filter(user -> FREE_USER.equals(user.getUserType())).isPresent()) {
        throw new UserAlreadyExistsException("Following BASIC_AUTH user with email already exists.");
      }

      UserWithPassword existingDraft = (UserWithPassword) userByEmail.get();
      return verificationCodeService.sendVerificationCodeToDraftUser(existingDraft);

    } else {
      UserWithPassword newDraft = createNewDraftUser(newUserRequest);
      return verificationCodeService.sendVerificationCodeToDraftUser(newDraft);
    }
  }

  @Transactional
  public UserDto activateDraftBy(ActivateUserRequest request, String clientId) {
    hasText(request.getEmail(), "Draft email is mandatory");
    hasText(request.getVerificationCode(), "Verification code must be valid");
    hasText(clientId, "Client Id must be valid");

    VerificationCodeDto verificationCode = verificationCodeService.findVerificationCode(request.getVerificationCode(), REGISTRATION);

    BaseUser user = findBasicUserByEmail(request.getEmail())
        .filter(u -> DRAFT_USER == u.getUserDetails().getType())
        .filter(not(BaseUser::getEnabled))
        .orElseThrow(() -> new UserRegistrationException(DRAFT_NOT_FOUND, "Could not find any eligible draft user with the following email"));

    if (!verificationCode.getUser().getId().equals(user.getId())) {
      throw new UserRegistrationException(VALIDATION_CODE_NOT_VALID, "Validation Code is assigned to different user");
    }

    log.info("Received request from `{}` client to activate DRAFT User `{}`", clientId, user.getId());

    user.activate();
    authoritiesService.insertUserAuthorities(user.getId(), FREE_USER);
    verificationCodeService.useCode(verificationCode.getId());

    log.info("User `{}` is now active and can now login to the system via BASIC_AUTH protocol", user.getId());
    return user.toDto();
  }

  @Transactional
  public CommunicationDto resetPasswordFor(String email) {
    log.info("User `{}` requests Password Reset", maskEmail(email));
    hasText(email, "Email must be valid");

    UserWithPassword user = (UserWithPassword) findBasicUserByEmail(email)
        .filter(u -> DRAFT_USER != u.getUserDetails().getType())
        .filter(BaseUser::getEnabled)
        .orElseThrow(() -> new PasswordResetException(USER_NOT_FOUND, "Cannot request reset password. User must be an active one"));

    log.info("Request received to reset BASIC_AUTH user ({} - {}) password.", maskEmail(email), user.getId());
    return verificationCodeService.sendResetPasswordToActiveUser(user);
  }

  @Transactional
  public void changeUserPassword(String email, String code, String newPassword) {
    log.info("User `{}` tries to change password with verification_code", maskEmail(email));

    hasText(email, "Email is required!");
    hasText(code, "Verification_Code is required!");
    hasText(newPassword, "New Password is required!");

    UserWithPassword user = (UserWithPassword) findBasicUserByEmail(email)
        .filter(u -> DRAFT_USER != u.getUserDetails().getType())
        .filter(BaseUser::getEnabled)
        .orElseThrow(() -> new PasswordResetException(USER_NOT_FOUND, "Cannot change user password. User must be an active one"));

    VerificationCodeDto verificationCode = verificationCodeService.findVerificationCode(code, RESET_PASSWORD);

    if (!verificationCode.getUser().getId().equals(user.getId())) {
      throw new PasswordResetException(VALIDATION_CODE_NOT_VALID, "Validation Code is assigned to different user");
    }

    userRepository.updateUserPassword(user.getId(), passwordEncoder.encode(newPassword));
    verificationCodeService.useCode(verificationCode.getId());

    log.info("User `{}` have successfully changed it's password. Verification Code with id {} is marked as used", maskEmail(email), verificationCode.getId());
  }

  @Transactional
  public UserInfoDto updateUserInfo(UUID userId, UserInfoUpdateDto userInfo) {
    return userRepository.findById(userId)
        .filter(BaseUser::getEnabled)
        .map(user -> user.updateWith(userInfo))
        .map(userRepository::save)
        .map(user -> user.toUserInfo(getUserPremiumFeatures(user)))
        .orElseThrow(() -> new UserNotExistsException(USER_NOT_FOUND, "Active User with following id `" + userId + " ` does not exists"));
  }

  @Transactional
  public void updateUserPoints(UUID userId, int points) {
    if (points == 0) {
      log.debug("No points to add or remove for user {}", userId);
      return;
    }

    userRepository.findById(userId)
        .orElseThrow(() -> new UserNotExistsException(USER_NOT_FOUND, "Active User with following id `" + userId + " ` does not exists"));
    userRepository.updateUserPoints(userId, points);
  }

  @Transactional
  public void deleteUser(String authenticationToken) {
    optOutService.optoutUser(authenticationToken);
  }

  private void checkIfUserExists(String externalUserId, OAuth2Provider oauth2Provider) {
    log.debug("Searching for User authenticated by `{}` with externalId - `{}`", oauth2Provider, externalUserId);

    if (userRepository.findByExternalIdAndAuthProvider(externalUserId, oauth2Provider).isPresent()) {
      throw new UserAlreadyExistsException("Following External user with externalID `%s` already exists.".formatted(externalUserId));
    }
  }

  private Set<PremiumFeature> getUserPremiumFeatures(BaseUser user) {
    return authoritiesService.getUserAuthorities(user.getId())
        .stream()
        .map(PremiumFeature::fromAuthority)
        .filter(not(NON::equals))
        .collect(toSet());
  }

  private UserWithPassword createNewDraftUser(NewUserRequest userRequest) {
    UserDetails userDetails = UserDetails.builder()
        .type(DRAFT_USER)
        .email(userRequest.getEmail())
        .build();

    String encodedPassword = passwordEncoder.encode(userRequest.getPassword());

    UserWithPassword user = newDraftFrom(encodedPassword, userDetails);
    return userRepository.saveAndFlush(user);
  }

}