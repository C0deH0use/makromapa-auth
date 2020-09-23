package pl.code.house.makro.mapa.auth.domain.user;

import static java.util.function.Predicate.not;
import static org.apache.commons.lang3.StringUtils.isBlank;
import static org.springframework.util.Assert.hasText;
import static pl.code.house.makro.mapa.auth.domain.user.CodeType.REGISTRATION;
import static pl.code.house.makro.mapa.auth.domain.user.CodeType.RESET_PASSWORD;
import static pl.code.house.makro.mapa.auth.domain.user.ExternalUser.newUserFrom;
import static pl.code.house.makro.mapa.auth.domain.user.OAuth2Provider.BASIC_AUTH;
import static pl.code.house.makro.mapa.auth.domain.user.OAuth2Provider.FACEBOOK;
import static pl.code.house.makro.mapa.auth.domain.user.OAuth2Provider.fromIssuer;
import static pl.code.house.makro.mapa.auth.domain.user.UserType.DRAFT_USER;
import static pl.code.house.makro.mapa.auth.domain.user.UserType.FREE_USER;
import static pl.code.house.makro.mapa.auth.domain.user.UserType.PREMIUM_USER;
import static pl.code.house.makro.mapa.auth.domain.user.UserWithPassword.newDraftFrom;
import static pl.code.house.makro.mapa.auth.error.UserOperationError.DRAFT_NOT_FOUND;
import static pl.code.house.makro.mapa.auth.error.UserOperationError.USER_NOT_FOUND;
import static pl.code.house.makro.mapa.auth.error.UserOperationError.VALIDATION_CODE_NOT_VALID;

import java.util.Optional;
import lombok.RequiredArgsConstructor;
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
import pl.code.house.makro.mapa.auth.domain.user.dto.VerificationCodeDto;
import pl.code.house.makro.mapa.auth.error.InsufficientUserDetailsException;
import pl.code.house.makro.mapa.auth.error.NewTermsAndConditionsNotApprovedException;
import pl.code.house.makro.mapa.auth.error.PasswordResetException;
import pl.code.house.makro.mapa.auth.error.UserAlreadyExistsException;
import pl.code.house.makro.mapa.auth.error.UserRegistrationException;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserFacade {

  private final PasswordEncoder passwordEncoder;

  private final UserRepository userRepository;

  private final UserAuthoritiesService userAuthoritiesService;

  private final TermsAndConditionsRepository termsRepository;

  private final UserOptOutService optOutService;

  private final VerificationCodeService verificationCodeService;

  public static String maskEmail(String email) {
    return email.replaceAll("(^[^@]{3}|(?!^)\\G)[^@]", "$1*");
  }

  @Transactional
  public UserDto findUserByToken(Jwt token) {
    String externalUserId = tryGetExternalUserId(token);
    OAuth2Provider oauth2Provider = fromIssuer(token.getClaim("iss"));
    log.debug("Searching for User authenticated by `{}` with externalId - `{}`", oauth2Provider, externalUserId);

    BaseUser user = userRepository.findByExternalIdAndAuthProvider(externalUserId)
        .map(u -> u.updateWith(parseUserDetails(token)))
        .orElseGet(() -> createNewExternalUser(token));

    return checkTcAndReturnDto(user);
  }

  @Transactional
  public UserDto findUserByProfile(User userProfile) {
    String externalUserId = userProfile.getId();
    log.debug("Searching for User authenticated by `{}` with externalId - `{}`", FACEBOOK, externalUserId);

    BaseUser user = userRepository.findByExternalIdAndAuthProvider(externalUserId)
        .map(u -> u.updateWith(parseUserDetails(userProfile)))
        .orElseGet(() -> createNewFacebookUser(userProfile));

    return checkTcAndReturnDto(user);
  }

  @Transactional
  public CommunicationDto registerNewUser(NewUserRequest newUserRequest) {
    log.info("Registering new DRAFT User `{}` as BASIC_AUTH authentication provider", newUserRequest.getEmail());

    Optional<BaseUser> userByEmail = userRepository.findUserWithPasswordByUserEmail(newUserRequest.getEmail());
    if (userByEmail.isPresent()) {
      if (userByEmail.filter(user -> FREE_USER.equals(user.getUserType())).isPresent()) {
        throw new UserAlreadyExistsException("Following BASIC_AUTH user with email already exists.");
      }

      UserWithPassword existingDraft = (UserWithPassword) userByEmail.get();
      if (verificationCodeService.findVerificationCode(existingDraft.getId(), REGISTRATION).isPresent()) {
        throw new UserAlreadyExistsException("Following BASIC_AUTH user with email has valid verification_code.");
      }
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

    BaseUser user = userRepository.findUserWithPasswordByUserEmail(request.getEmail())
        .filter(u -> DRAFT_USER == u.getUserDetails().getType())
        .filter(not(BaseUser::getEnabled))
        .orElseThrow(() -> new UserRegistrationException(DRAFT_NOT_FOUND, "Could not find any eligible draft user with the following email"));

    if (!verificationCode.getUser().getId().equals(user.getId())) {
      throw new UserRegistrationException(VALIDATION_CODE_NOT_VALID, "Validation Code is assigned to different user");
    }

    log.info("Received request from `{}` client to activate DRAFT User `{}`", clientId, user.getId());

    user.activate();
    userAuthoritiesService.insertUserAuthorities(user.getId(), FREE_USER);
    verificationCodeService.useCode(verificationCode.getId());

    log.info("User `{}` is now active and can now login to the system via BASIC_AUTH protocol", user.getId());
    return user.toDto();
  }

  @Transactional
  public CommunicationDto resetPasswordFor(String email) {
    log.info("User `{}` requests Password Reset", maskEmail(email));
    hasText(email, "Email must be valid");

    UserWithPassword user = (UserWithPassword) userRepository.findUserWithPasswordByUserEmail(email)
        .filter(u -> DRAFT_USER != u.getUserDetails().getType())
        .filter(BaseUser::getEnabled)
        .orElseThrow(() -> new PasswordResetException(USER_NOT_FOUND, "Cannot reset password for DRAFT user. Please register first."));

    log.info("Request received to reset BASIC_AUTH user ({} - {}) password.", maskEmail(email), user.getId());
    return verificationCodeService.sendResetPasswordToActiveUser(user);
  }

  @Transactional
  public void changeUserPassword(String email, String code, String newPassword) {
    log.info("User `{}` tries to change password with verification_code", maskEmail(email));

    hasText(email, "Email is required!");
    hasText(code, "Verification_Code is required!");
    hasText(newPassword, "New Password is required!");

    UserWithPassword user = (UserWithPassword) userRepository.findUserWithPasswordByUserEmail(email)
        .filter(u -> DRAFT_USER != u.getUserDetails().getType())
        .filter(BaseUser::getEnabled)
        .orElseThrow(() -> new PasswordResetException(USER_NOT_FOUND, "Cannot reset password for DRAFT user. Please register first."));

    VerificationCodeDto verificationCode = verificationCodeService.findVerificationCode(code, RESET_PASSWORD);

    if (!verificationCode.getUser().getId().equals(user.getId())) {
      throw new PasswordResetException(VALIDATION_CODE_NOT_VALID, "Validation Code is assigned to different user");
    }

    userRepository.updateUserPassword(user.getId(), passwordEncoder.encode(newPassword));
    verificationCodeService.useCode(verificationCode.getId());

    log.info("User `{}` have successfully changed it's password. Verification Code with id {} is marked as used", maskEmail(email), verificationCode.getId());
  }

  @Transactional
  public void deleteUser(String authenticationToken) {
    optOutService.optoutUser(authenticationToken);
  }

  private UserDto checkTcAndReturnDto(BaseUser user) {
    if (PREMIUM_USER == user.getUserDetails().getType()) {
      TermsAndConditions latestTnC = termsRepository.findFirstByOrderByLastUpdatedDesc();
      boolean userNotApprovedLatestTnC = latestTnC.getId().equals(user.getTermsAndConditionsId());

      if (!userNotApprovedLatestTnC) {
        throw new NewTermsAndConditionsNotApprovedException("New terms and conditions are required for user approval");
      }
    }
    return user.toDto();
  }

  private ExternalUser createNewExternalUser(Jwt jwtPrincipal) {
    String externalId = tryGetExternalUserId(jwtPrincipal);
    OAuth2Provider oauth2Provider = fromIssuer(jwtPrincipal.getClaim("iss"));

    UserDetails userDetails = parseUserDetails(jwtPrincipal);

    return createNewFreeUser(externalId, oauth2Provider, userDetails);
  }

  private ExternalUser createNewFacebookUser(User profile) {
    String externalId = profile.getId();
    OAuth2Provider oauth2Provider = FACEBOOK;

    UserDetails userDetails = parseUserDetails(profile);
    return createNewFreeUser(externalId, oauth2Provider, userDetails);
  }

  private ExternalUser createNewFreeUser(String externalId, OAuth2Provider oauth2Provider, UserDetails userDetails) {
    log.info("Creating new FREE_USER `{}`. Authentication provider: {}", externalId, oauth2Provider);

    ExternalUser externalUser = userRepository.saveAndFlush(newUserFrom(oauth2Provider, userDetails, externalId));
    userAuthoritiesService.insertUserAuthorities(externalUser.getId(), FREE_USER);
    return externalUser;
  }

  private UserDetails parseUserDetails(Jwt jwtPrincipal) {
    return UserDetails.builder()
        .type(FREE_USER)
        .name(jwtPrincipal.getClaim("name"))
        .email(jwtPrincipal.getClaim("email"))
        .surname(jwtPrincipal.getClaim("family_name"))
        .picture(jwtPrincipal.getClaim("picture"))
        .build();
  }

  private UserDetails parseUserDetails(User profile) {
    return UserDetails.builder()
        .type(FREE_USER)
        .name(profile.getFirstName())
        .email(profile.getEmail())
        .surname(profile.getLastName())
        .build();
  }

  private UserWithPassword createNewDraftUser(NewUserRequest userRequest) {
    UserDetails userDetails = UserDetails.builder()
        .type(DRAFT_USER)
        .email(userRequest.getEmail())
        .build();

    String encodedPassword = passwordEncoder.encode(userRequest.getPassword());

    UserWithPassword user = newDraftFrom(BASIC_AUTH, encodedPassword, userDetails);
    return userRepository.saveAndFlush(user);
  }

  private String tryGetExternalUserId(Jwt principal) {
    String externalId = principal.getClaim("sub");
    if (isBlank(externalId)) {
      throw new InsufficientUserDetailsException("Authentication Token does not contain required data > external user Id is missing");
    }

    return externalId;
  }

}