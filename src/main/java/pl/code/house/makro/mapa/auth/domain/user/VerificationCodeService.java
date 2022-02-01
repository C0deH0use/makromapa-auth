package pl.code.house.makro.mapa.auth.domain.user;

import static java.time.ZonedDateTime.now;
import static org.apache.commons.lang3.RandomStringUtils.randomNumeric;
import static org.springframework.util.Assert.hasText;
import static org.springframework.util.Assert.isTrue;
import static pl.code.house.makro.mapa.auth.domain.user.CodeType.REGISTRATION;
import static pl.code.house.makro.mapa.auth.domain.user.CodeType.RESET_PASSWORD;
import static pl.code.house.makro.mapa.auth.domain.user.CommunicationProtocol.EMAIL;
import static pl.code.house.makro.mapa.auth.domain.user.OAuth2Provider.BASIC_AUTH;
import static pl.code.house.makro.mapa.auth.domain.user.UserType.DRAFT_USER;
import static pl.code.house.makro.mapa.auth.error.UserOperationError.VALIDATION_CODE_NOT_FOUND;

import java.time.Clock;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.thymeleaf.context.Context;
import pl.code.house.makro.mapa.auth.domain.mail.EmailService;
import pl.code.house.makro.mapa.auth.domain.mail.dto.MessageDetails;
import pl.code.house.makro.mapa.auth.domain.mail.dto.RegistrationMessageDetails;
import pl.code.house.makro.mapa.auth.domain.mail.dto.ResetPasswordMessageDetails;
import pl.code.house.makro.mapa.auth.domain.user.dto.CommunicationDto;
import pl.code.house.makro.mapa.auth.domain.user.dto.VerificationCodeDto;
import pl.code.house.makro.mapa.auth.error.UserRegistrationException;

@Slf4j
@Service
@RequiredArgsConstructor
class VerificationCodeService {

  private static final DateTimeFormatter EXPIRY_DATE_FORMAT = DateTimeFormatter.ofPattern("dd-MM-yyyy hh:mm");
  private static final String COULD_NOT_FIND_VERIFICATION_CODE_MSG = "Could not find any VALID verificationCode with such code.";
  private final Clock clock;

  private final UserActivationProperties userActivationProperties;

  private final PasswordResetProperties passwordResetProperties;

  private final UserVerificationCodeRepository repository;

  private final EmailService emailService;

  @Transactional
  void useCode(UUID codeId) {
    log.info("Marking verificationCode {} as disabled -> Used in a User ACTIVATION process.", codeId);

    repository.useCode(codeId);
  }

  @Transactional
  CommunicationDto sendVerificationCodeToDraftUser(UserWithPassword draftUser) {
    log.info("Preparing verification code for draft user - {}", draftUser.getId());

    validateUser(draftUser);
    validateDraft(draftUser);

    findVerificationCode(draftUser.getId(), REGISTRATION)
        .map(VerificationCodeDto::getId)
        .ifPresent(this::deleteCode);

    UserVerificationCode verificationCode = buildVerificationCodeWith(draftUser, REGISTRATION);

    sendActivationEmailToUser(verificationCode, draftUser);

    log.debug("Storing new verificationCode `{}` ... with expiry date set to {}", verificationCode.getCode(), verificationCode.getExpiresOn());
    repository.save(verificationCode);
    return new CommunicationDto(draftUser.getId(), REGISTRATION, draftUser.getUserDetails().getEmail(), EMAIL);
  }

  @Transactional
  CommunicationDto sendResetPasswordToActiveUser(UserWithPassword user) {
    log.info("Preparing Reset code for user - {}", user.getId());

    validateUser(user);
    validateActiveUser(user);

    findVerificationCode(user.getId(), RESET_PASSWORD)
        .map(VerificationCodeDto::getId)
        .ifPresent(this::deleteCode);

    UserVerificationCode verificationCode = buildVerificationCodeWith(user, RESET_PASSWORD);

    sendResetPasswordEmailToUser(verificationCode, user);

    log.debug("Storing verificationCode `{}` ... with expiry date set to {}", verificationCode.getCode(), verificationCode.getExpiresOn());
    repository.save(verificationCode);
    return new CommunicationDto(user.getId(), RESET_PASSWORD, user.getUserDetails().getEmail(), EMAIL);
  }

  private void deleteCode(UUID codeId) {
    log.info("Removing existing VerificationCode with id - {}", codeId.toString());
    repository.deleteById(codeId);
  }

  Optional<VerificationCodeDto> findVerificationCode(UUID userId, CodeType codeType) {
    log.info("Searching for valid verification code assigned to user - `{}`", userId);

    return repository.findByUserIdAndCodeType(userId, codeType)
        .stream()
        .filter(UserVerificationCode::getEnabled)
        .filter(vc -> vc.getExpiresOn().isAfter(now(clock)))
        .map(UserVerificationCode::toDto)
        .findFirst();
  }

  VerificationCodeDto findVerificationCode(String verificationCode, CodeType codeType) {
    log.info("Searching for verification code `{}`, and check if it is still a valid one", verificationCode);

    return repository.findActiveCode(verificationCode, now(clock), codeType)
        .map(UserVerificationCode::toDto)
        .orElseThrow(() -> new UserRegistrationException(VALIDATION_CODE_NOT_FOUND, COULD_NOT_FIND_VERIFICATION_CODE_MSG));
  }

  private void sendActivationEmailToUser(UserVerificationCode verificationCode, UserWithPassword draftUser) {
    log.info("Sending REGISTRATION EMAIL to new user {}", draftUser.getId());

    Context messageCtx = new Context();
    messageCtx.setVariable("verification_code", verificationCode.getCode());
    messageCtx.setVariable("expiry_date", EXPIRY_DATE_FORMAT.format(verificationCode.getExpiresOn()));

    MessageDetails messageDetails = new RegistrationMessageDetails(userActivationProperties.getMailSubject(),
        draftUser.getUserDetails().getEmail(),
        messageCtx);
    emailService.sendHtmlMail(messageDetails);
  }

  private void sendResetPasswordEmailToUser(UserVerificationCode verificationCode, UserWithPassword draftUser) {
    log.info("Sending RESET PASSWORD EMAIL to user {}", draftUser.getId());

    Context messageCtx = new Context();
    messageCtx.setVariable("verification_code", verificationCode.getCode());
    messageCtx.setVariable("expiry_date", EXPIRY_DATE_FORMAT.format(verificationCode.getExpiresOn()));
    messageCtx.setVariable("user_name", draftUser.getUserDetails().getEmail());

    MessageDetails messageDetails = new ResetPasswordMessageDetails(passwordResetProperties.getMailSubject(),
        draftUser.getUserDetails().getEmail(),
        messageCtx);
    emailService.sendHtmlMail(messageDetails);
  }

  private UserVerificationCode buildVerificationCodeWith(UserWithPassword draftUser, CodeType codeType) {
    ZonedDateTime expiresOn = now(clock).plusHours(passwordResetProperties.getExpiresOn());
    String code = randomNumeric(4) + "29";

    log.debug("Building new verificationCode `{}` ... with expiry date set to {}", code, expiresOn);
    return UserVerificationCode.builder()
        .id(UUID.randomUUID())
        .user(draftUser)
        .code(code)
        .codeType(codeType)
        .enabled(true)
        .expiresOn(expiresOn)
        .build();
  }

  private void validateUser(UserWithPassword user) {
    isTrue(BASIC_AUTH == user.getProvider(), "User need to set for Basic Authentication");

    hasText(user.getPassword(), "User must have password set");
    hasText(user.getUserDetails().getEmail(), "User must have a username -> email.");
  }

  private void validateDraft(UserWithPassword user) {
    isTrue(DRAFT_USER == user.getUserDetails().getType(), "User must be of DRAFT Type");
    isTrue(!user.getEnabled(), "User must not be active");
  }

  private void validateActiveUser(UserWithPassword user) {
    isTrue(DRAFT_USER != user.getUserDetails().getType(), "User must not be DRAFT Type");
    isTrue(user.getEnabled(), "User must be active");
  }
}
