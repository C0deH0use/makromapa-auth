package pl.code.house.makro.mapa.auth.domain.user;

import static java.util.function.Predicate.not;
import static org.apache.commons.lang3.StringUtils.isBlank;
import static org.springframework.util.Assert.hasText;
import static pl.code.house.makro.mapa.auth.domain.user.CodeType.REGISTRATION;
import static pl.code.house.makro.mapa.auth.domain.user.CodeType.RESET_PASSWORD;
import static pl.code.house.makro.mapa.auth.domain.user.ExternalUser.newUserFrom;
import static pl.code.house.makro.mapa.auth.domain.user.OAuth2Provider.BASIC_AUTH;
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
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
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

  private final VerificationCodeService verificationCodeService;

  @Transactional
  public UserDto findUserByToken(Jwt token) {
    String externalUserId = tryGetExternalUserId(token);
    OAuth2Provider oauth2Provider = fromIssuer(token.getClaim("iss"));
    log.debug("Searching for User authenticated by `{}` with externalId - `{}`", oauth2Provider, externalUserId);

    BaseUser user = userRepository.findByExternalIdAndAuthProvider(externalUserId)
        .map(u -> u.updateWith(parseUserDetails(token)))
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
  public UserDto activateDraftBy(String code, String clientId) {
    hasText(code, "Activation code must be valid");
    hasText(clientId, "Client Id must be valid");

    VerificationCodeDto verificationCode = verificationCodeService.findVerificationCode(code, REGISTRATION);

    BaseUser user = userRepository.findById(verificationCode.getUser().getId())
        .filter(u -> DRAFT_USER == u.getUserDetails().getType())
        .filter(not(BaseUser::getEnabled))
        .orElseThrow(() -> new UserRegistrationException(DRAFT_NOT_FOUND, "Disabled DRAFT user that was assigned to the Activation Code was not found."));

    if (!verificationCode.getUser().getId().equals(user.getId())) {
      throw new UserRegistrationException(VALIDATION_CODE_NOT_VALID, "Validation Code is assigned to different user");
    }

    log.info("Received request from `{}` activate DRAFT User `{}`", clientId, user.getId());

    user.activate();
    userAuthoritiesService.insertUserAuthorities(user.getId(), FREE_USER);
    verificationCodeService.useCode(verificationCode.getId());

    log.info("User `{}` is now active and can no login to the system via BASIC_AUTH protocol", user.getId());
    return user.toDto();
  }

  @Transactional
  public CommunicationDto resetPasswordFor(String email) {
    log.info("User `{}` requests Password Reset", email);
    hasText(email, "Email must be valid");

    UserWithPassword user = (UserWithPassword) userRepository.findUserWithPasswordByUserEmail(email)
        .filter(u -> DRAFT_USER != u.getUserDetails().getType())
        .filter(BaseUser::getEnabled)
        .orElseThrow(() -> new PasswordResetException(USER_NOT_FOUND, "Cannot reset password for DRAFT user. Please register first."));

    log.info("Request received to reset BASIC_AUTH user ({} - {}) password.", email, user.getId());
    return verificationCodeService.sendResetPasswordToActiveUser(user);
  }

  @Transactional
  public void changeUserPassword(String email, String code, String newPassword) {
    log.info("User `{}` tries to change password with verification_code", email);

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

    log.info("User `{}` have successfully changed it's password. Verification Code with id {} is marked as used", email, verificationCode.getId());
  }

  private ExternalUser createNewFreeUser(Jwt jwtPrincipal) {
    String externalId = tryGetExternalUserId(jwtPrincipal);
    OAuth2Provider oauth2Provider = fromIssuer(jwtPrincipal.getClaim("iss"));

    UserDetails userDetails = parseUserDetails(jwtPrincipal);

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