package pl.code.house.makro.mapa.auth.domain.user;

import static org.springframework.util.Assert.hasText;
import static org.springframework.util.Assert.isTrue;
import static pl.code.house.makro.mapa.auth.domain.user.CommunicationProtocol.EMAIL;
import static pl.code.house.makro.mapa.auth.domain.user.OAuth2Provider.BASIC_AUTH;
import static pl.code.house.makro.mapa.auth.domain.user.UserType.DRAFT_USER;

import java.time.Clock;
import java.time.ZonedDateTime;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.thymeleaf.context.Context;
import pl.code.house.makro.mapa.auth.domain.mail.EmailService;
import pl.code.house.makro.mapa.auth.domain.mail.dto.RegistrationMessageDetails;
import pl.code.house.makro.mapa.auth.domain.user.dto.ActivationLinkDto;

@Slf4j
@Service
@RequiredArgsConstructor
class DraftActivationCodeService {

  private final Clock clock;

  private final UserActivationProperties properties;

  private final UserActivationCodeRepository repository;

  private final EmailService emailService;

  @Transactional
  ActivationLinkDto sendActivationCodeToDraftUser(UserWithPassword draftUser) {
    log.info("Preparing activation code for draft user - {}", draftUser.getId());

    validateUser(draftUser);
    UserActivationCode activationCode = buildActivationCodeFor(draftUser);

    sendActivationCodeToUser(activationCode, draftUser);

    log.debug("Storing new ActivationCode `{}` ... with expiry date set to {}", activationCode.getCode(), activationCode.getExpiresOn());
    repository.save(activationCode);
    return new ActivationLinkDto(draftUser.getId(), draftUser.getUserDetails().getEmail(), EMAIL, activationCode.getCode());
  }

  private void sendActivationCodeToUser(UserActivationCode activationCode, UserWithPassword draftUser) {
    log.info("Sending REGISTRATION EMAIL to new user {}", draftUser.getId());

    Context messageCtx = new Context();
    messageCtx.setVariable("name", "Testowe imię Marek");
    messageCtx.setVariable("activation_code", activationCode.getCode());

    RegistrationMessageDetails messageDetails = new RegistrationMessageDetails(properties.getMailSubject(), draftUser.getUserDetails().getEmail(), messageCtx);
    emailService.sendHtmlMail(messageDetails);
  }

  private UserActivationCode buildActivationCodeFor(UserWithPassword draftUser) {
    ZonedDateTime expiresOn = ZonedDateTime.now(clock).plusHours(properties.getExpiresOn());
    String code = UUID.randomUUID().toString();

    log.debug("Building new ActivationCode `{}` ... with expiry date set to {}", code, expiresOn);
    return UserActivationCode.builder()
        .id(UUID.randomUUID())
        .draftUser(draftUser)
        .code(code)
        .enabled(true)
        .expiresOn(expiresOn)
        .build();
  }

  private void validateUser(UserWithPassword user) {
    isTrue(BASIC_AUTH == user.getProvider(), "User need to set for Basic Authentication");

    hasText(user.getPassword(), "User must have password set");
    hasText(user.getUserDetails().getEmail(), "User must have a username -> email.");
    isTrue(DRAFT_USER == user.getUserDetails().getType(), "User must be of DRAFT Type");
    isTrue(!user.getEnabled(), "User must not be active");

    isTrue(repository.findActiveCodeByUserId(user.getId()).isEmpty(), "User has already an active activation_code registered");
  }

}
