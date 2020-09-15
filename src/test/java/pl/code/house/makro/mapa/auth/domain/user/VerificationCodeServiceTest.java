package pl.code.house.makro.mapa.auth.domain.user;

import static java.time.ZoneOffset.UTC;
import static java.time.ZonedDateTime.now;
import static org.apache.commons.lang3.RandomStringUtils.randomNumeric;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.atMostOnce;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static pl.code.house.makro.mapa.auth.domain.mail.EmailType.REGISTRATION;
import static pl.code.house.makro.mapa.auth.domain.mail.EmailType.RESET_PASSWORD;
import static pl.code.house.makro.mapa.auth.domain.user.CommunicationProtocol.EMAIL;
import static pl.code.house.makro.mapa.auth.domain.user.OAuth2Provider.BASIC_AUTH;
import static pl.code.house.makro.mapa.auth.domain.user.OAuth2Provider.GOOGLE;
import static pl.code.house.makro.mapa.auth.domain.user.TestUser.GOOGLE_NEW_USER;
import static pl.code.house.makro.mapa.auth.domain.user.UserType.DRAFT_USER;
import static pl.code.house.makro.mapa.auth.domain.user.UserType.FREE_USER;

import java.time.Clock;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import pl.code.house.makro.mapa.auth.domain.mail.EmailService;
import pl.code.house.makro.mapa.auth.domain.mail.dto.MessageDetails;
import pl.code.house.makro.mapa.auth.domain.user.dto.CommunicationDto;

@ExtendWith(MockitoExtension.class)
class VerificationCodeServiceTest {

  public static final UUID INVALID_CODE_ID = UUID.randomUUID();
  public static final UUID VALID_CODE_ID = UUID.randomUUID();
  public static final UUID VALID_RESET_PASSWORD_CODE_ID = UUID.randomUUID();
  public static final UUID INVALID_RESET_PASSWORD_CODE_ID = UUID.randomUUID();
  public static final String CODE = randomNumeric(6);
  private static final String PASSWORD = "DEFAULT_PASWD";
  private static final String USER_EMAIL = "email@domain.com";
  private static final int EXPIRES_AFTER = 10;

  private final Clock clock = Clock.fixed(Instant.parse("2020-07-03T10:15:30.00Z"), UTC);

  @Mock
  private UserVerificationCodeRepository repository;

  @Mock
  private EmailService emailService;

  private VerificationCodeService sut;

  @BeforeEach
  void setUp() {
    sut = new VerificationCodeService(clock, activationProperties(), resetProperties(), repository, emailService);
  }

  @Test
  @DisplayName("should create and send email with valid draft user")
  void shouldCreateAndSendEmailWithValidDraftUser() {
    //given
    UserWithPassword user = draftUser();
    given(repository.findByUserIdAndCodeType(GOOGLE_NEW_USER.getUserId(), CodeType.REGISTRATION)).willReturn(List.of());

    //when
    CommunicationDto linkDto = sut.sendVerificationCodeToDraftUser(user);

    //then
    ArgumentCaptor<MessageDetails> messageCapture = ArgumentCaptor.forClass(MessageDetails.class);
    ArgumentCaptor<UserVerificationCode> activationCodeCapture = ArgumentCaptor.forClass(UserVerificationCode.class);

    then(emailService).should(times(1)).sendHtmlMail(messageCapture.capture());
    MessageDetails captureValue = messageCapture.getValue();
    assertThat(captureValue.getType()).isEqualTo(REGISTRATION);
    assertThat(captureValue.getReceiver()).isEqualTo(USER_EMAIL);
    assertThat(captureValue.getContext().getVariable("verification_code")).isNotNull();

    assertThat(linkDto.getCommunicationChannel()).isEqualTo(EMAIL);
    assertThat(linkDto.getCommunicationTarget()).isEqualTo(USER_EMAIL);
    assertThat(linkDto.getCodeType()).isEqualTo(CodeType.REGISTRATION);

    then(repository).should(times(1)).save(activationCodeCapture.capture());
    UserVerificationCode verificationCode = activationCodeCapture.getValue();

    assertThat(verificationCode.getEnabled()).isTrue();
    assertThat(verificationCode.getUser()).isNotNull();
    assertThat(verificationCode.getCodeType()).isEqualTo(CodeType.REGISTRATION);
    assertThat(verificationCode.getExpiresOn()).isBeforeOrEqualTo(now(clock).plusHours(EXPIRES_AFTER));
    assertThat(verificationCode.getCode().length()).isEqualTo(6);
  }

  @Test
  @DisplayName("should remove old activation code and send new one to user")
  void shouldRemoveOldActivationCodeAndSendNewOneToUser() {
    //given
    UserWithPassword user = draftUser();
    given(repository.findByUserIdAndCodeType(GOOGLE_NEW_USER.getUserId(), CodeType.REGISTRATION)).willReturn(List.of(activationCode()));

    //when
    CommunicationDto linkDto = sut.sendVerificationCodeToDraftUser(user);

    //then
    assertThat(linkDto.getCommunicationChannel()).isEqualTo(EMAIL);
    assertThat(linkDto.getCommunicationTarget()).isEqualTo(USER_EMAIL);
    assertThat(linkDto.getCodeType()).isEqualTo(CodeType.REGISTRATION);

    then(repository).should(times(1)).deleteById(VALID_CODE_ID);

    ArgumentCaptor<UserVerificationCode> verificationCodeCapture = ArgumentCaptor.forClass(UserVerificationCode.class);
    then(repository).should(times(1)).save(verificationCodeCapture.capture());
    UserVerificationCode verificationCode = verificationCodeCapture.getValue();

    assertThat(verificationCode.getEnabled()).isTrue();
    assertThat(verificationCode.getUser()).isNotNull();
    assertThat(verificationCode.getCodeType()).isEqualTo(CodeType.REGISTRATION);
    assertThat(verificationCode.getExpiresOn()).isBeforeOrEqualTo(now(clock).plusHours(EXPIRES_AFTER));
    assertThat(verificationCode.getCode().length()).isEqualTo(6);

    ArgumentCaptor<MessageDetails> messageCapture = ArgumentCaptor.forClass(MessageDetails.class);
    then(emailService).should(times(1)).sendHtmlMail(messageCapture.capture());
    MessageDetails captureValue = messageCapture.getValue();
    assertThat(captureValue.getType()).isEqualTo(REGISTRATION);
    assertThat(captureValue.getReceiver()).isEqualTo(USER_EMAIL);
    assertThat(captureValue.getContext().getVariable("verification_code")).isNotNull();
  }

  @Test
  @DisplayName("should create new verification code for reset password email")
  void shouldCreateNewVerificationCodeAndSendResetPasswordEmail() {
    //given
    UserWithPassword user = activeUser();
    given(repository.findByUserIdAndCodeType(GOOGLE_NEW_USER.getUserId(), CodeType.RESET_PASSWORD)).willReturn(List.of());

    //when
    CommunicationDto linkDto = sut.sendResetPasswordToActiveUser(user);

    //then
    ArgumentCaptor<MessageDetails> messageCapture = ArgumentCaptor.forClass(MessageDetails.class);
    ArgumentCaptor<UserVerificationCode> verificationCodeCapture = ArgumentCaptor.forClass(UserVerificationCode.class);

    then(emailService).should(times(1)).sendHtmlMail(messageCapture.capture());
    MessageDetails captureValue = messageCapture.getValue();
    assertThat(captureValue.getType()).isEqualTo(RESET_PASSWORD);
    assertThat(captureValue.getReceiver()).isEqualTo(USER_EMAIL);
    assertThat(captureValue.getContext().getVariable("verification_code")).isNotNull();

    assertThat(linkDto.getCommunicationChannel()).isEqualTo(EMAIL);
    assertThat(linkDto.getCommunicationTarget()).isEqualTo(USER_EMAIL);
    assertThat(linkDto.getCodeType()).isEqualTo(CodeType.RESET_PASSWORD);

    then(repository).should(times(1)).save(verificationCodeCapture.capture());
    UserVerificationCode verificationCode = verificationCodeCapture.getValue();

    assertThat(verificationCode.getEnabled()).isTrue();
    assertThat(verificationCode.getUser()).isNotNull();
    assertThat(verificationCode.getCodeType()).isEqualTo(CodeType.RESET_PASSWORD);
    assertThat(verificationCode.getExpiresOn()).isBeforeOrEqualTo(now(clock).plusHours(EXPIRES_AFTER));
    assertThat(verificationCode.getCode().length()).isEqualTo(6);
  }

  @Test
  @DisplayName("should delete and store new code for reset password if there is already one valid existing for user")
  void shouldDeleteAndStoreNewCodeForResetPasswordIfThereIsAlreadyOneValidExistingForUser() {
    //given
    UserWithPassword user = activeUser();
    given(repository.findByUserIdAndCodeType(GOOGLE_NEW_USER.getUserId(), CodeType.RESET_PASSWORD)).willReturn(List.of(resetPasswordCode()));

    //when
    CommunicationDto linkDto = sut.sendResetPasswordToActiveUser(user);

    //then
    assertThat(linkDto.getCommunicationChannel()).isEqualTo(EMAIL);
    assertThat(linkDto.getCommunicationTarget()).isEqualTo(USER_EMAIL);
    assertThat(linkDto.getCodeType()).isEqualTo(CodeType.RESET_PASSWORD);

    then(repository).should(times(1)).deleteById(VALID_RESET_PASSWORD_CODE_ID);

    ArgumentCaptor<UserVerificationCode> verificationCodeCapture = ArgumentCaptor.forClass(UserVerificationCode.class);
    then(repository).should(times(1)).save(verificationCodeCapture.capture());
    UserVerificationCode verificationCode = verificationCodeCapture.getValue();

    assertThat(verificationCode.getEnabled()).isTrue();
    assertThat(verificationCode.getUser()).isNotNull();
    assertThat(verificationCode.getCodeType()).isEqualTo(CodeType.RESET_PASSWORD);
    assertThat(verificationCode.getExpiresOn()).isBeforeOrEqualTo(now(clock).plusHours(EXPIRES_AFTER));
    assertThat(verificationCode.getCode().length()).isEqualTo(6);

    ArgumentCaptor<MessageDetails> messageCapture = ArgumentCaptor.forClass(MessageDetails.class);
    then(emailService).should(times(1)).sendHtmlMail(messageCapture.capture());
    MessageDetails captureValue = messageCapture.getValue();
    assertThat(captureValue.getType()).isEqualTo(RESET_PASSWORD);
    assertThat(captureValue.getReceiver()).isEqualTo(USER_EMAIL);
    assertThat(captureValue.getContext().getVariable("verification_code")).isNotNull();
  }

  @Test
  @DisplayName("create new verification code for reset password if there is one but not valid")
  void createNewVerificationCodeForResetPasswordIfThereIsOneButNotValid() {
    //given
    UserWithPassword user = activeUser();
    given(repository.findByUserIdAndCodeType(GOOGLE_NEW_USER.getUserId(), CodeType.RESET_PASSWORD)).willReturn(List.of(resetPasswordCode()));

    //when
    CommunicationDto linkDto = sut.sendResetPasswordToActiveUser(user);

    //then
    assertThat(linkDto.getCommunicationChannel()).isEqualTo(EMAIL);
    assertThat(linkDto.getCommunicationTarget()).isEqualTo(USER_EMAIL);
    assertThat(linkDto.getCodeType()).isEqualTo(CodeType.RESET_PASSWORD);

    then(repository).should(times(1)).deleteById(VALID_RESET_PASSWORD_CODE_ID);

    ArgumentCaptor<UserVerificationCode> verificationCodeCapture = ArgumentCaptor.forClass(UserVerificationCode.class);
    then(repository).should(times(1)).save(verificationCodeCapture.capture());
    UserVerificationCode verificationCode = verificationCodeCapture.getValue();

    assertThat(verificationCode.getEnabled()).isTrue();
    assertThat(verificationCode.getUser()).isNotNull();
    assertThat(verificationCode.getCodeType()).isEqualTo(CodeType.RESET_PASSWORD);
    assertThat(verificationCode.getExpiresOn()).isBeforeOrEqualTo(now(clock).plusHours(EXPIRES_AFTER));
    assertThat(verificationCode.getCode().length()).isEqualTo(6);

    ArgumentCaptor<MessageDetails> messageCapture = ArgumentCaptor.forClass(MessageDetails.class);
    then(emailService).should(times(1)).sendHtmlMail(messageCapture.capture());
    MessageDetails captureValue = messageCapture.getValue();
    assertThat(captureValue.getType()).isEqualTo(RESET_PASSWORD);
    assertThat(captureValue.getReceiver()).isEqualTo(USER_EMAIL);
    assertThat(captureValue.getContext().getVariable("verification_code")).isNotNull();
  }

  @Test
  @DisplayName("do not remove verification code if is not linked to password reset")
  void doNotRemoveVerificationCodeIfIsNotLinkedToPasswordReset() {
    //given
    UserWithPassword user = activeUser();
    given(repository.findByUserIdAndCodeType(GOOGLE_NEW_USER.getUserId(), CodeType.RESET_PASSWORD)).willReturn(List.of());

    //when
    CommunicationDto linkDto = sut.sendResetPasswordToActiveUser(user);

    //then
    assertThat(linkDto.getCommunicationChannel()).isEqualTo(EMAIL);
    assertThat(linkDto.getCommunicationTarget()).isEqualTo(USER_EMAIL);
    assertThat(linkDto.getCodeType()).isEqualTo(CodeType.RESET_PASSWORD);

    then(repository).should(never()).deleteById(VALID_CODE_ID);

    ArgumentCaptor<UserVerificationCode> verificationCodeCapture = ArgumentCaptor.forClass(UserVerificationCode.class);
    then(repository).should(atMostOnce()).save(verificationCodeCapture.capture());
    UserVerificationCode verificationCode = verificationCodeCapture.getValue();

    assertThat(verificationCode.getEnabled()).isTrue();
    assertThat(verificationCode.getUser()).isNotNull();
    assertThat(verificationCode.getCodeType()).isEqualTo(CodeType.RESET_PASSWORD);
    assertThat(verificationCode.getExpiresOn()).isBeforeOrEqualTo(now(clock).plusHours(EXPIRES_AFTER));
    assertThat(verificationCode.getCode().length()).isEqualTo(6);

    ArgumentCaptor<MessageDetails> messageCapture = ArgumentCaptor.forClass(MessageDetails.class);
    then(emailService).should(times(1)).sendHtmlMail(messageCapture.capture());
    MessageDetails captureValue = messageCapture.getValue();
    assertThat(captureValue.getType()).isEqualTo(RESET_PASSWORD);
    assertThat(captureValue.getReceiver()).isEqualTo(USER_EMAIL);
    assertThat(captureValue.getContext().getVariable("verification_code")).isNotNull();
  }

  @Test
  @DisplayName("throw if user is not a draft user")
  void throwIfUserIsNotADraftUser() {
    //given
    UserDetails userDetail = new UserDetails(null, null, USER_EMAIL, null, FREE_USER);
    UserWithPassword user = new UserWithPassword(GOOGLE_NEW_USER.getUserId(), PASSWORD, false, null, BASIC_AUTH, userDetail);

    //when & then
    assertThatThrownBy(() -> sut.sendVerificationCodeToDraftUser(user))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("must be of DRAFT");
  }

  @Test
  @DisplayName("throw if user does not have email")
  void throwIfUserDoesNotHaveEmail() {
    //given
    UserDetails userDetail = new UserDetails(null, null, " ", null, DRAFT_USER);
    UserWithPassword user = new UserWithPassword(GOOGLE_NEW_USER.getUserId(), PASSWORD, false, null, BASIC_AUTH, userDetail);

    //when & then
    assertThatThrownBy(() -> sut.sendVerificationCodeToDraftUser(user))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("must have a username");
  }

  @Test
  @DisplayName("throw if user does not have a password stored")
  void throwIfUserDoesNotHaveAPasswordStored() {
    //given
    UserDetails userDetail = new UserDetails(null, null, USER_EMAIL, null, DRAFT_USER);
    UserWithPassword user = new UserWithPassword(GOOGLE_NEW_USER.getUserId(), null, false, null, BASIC_AUTH, userDetail);

    //when & then
    assertThatThrownBy(() -> sut.sendVerificationCodeToDraftUser(user))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("have password");
  }

  @Test
  @DisplayName("throw if user is not set for BASIC_AUTH")
  void throwIfUserIsNotSetForBasicAuth() {
    //given
    UserDetails userDetail = new UserDetails(null, null, USER_EMAIL, null, DRAFT_USER);
    UserWithPassword user = new UserWithPassword(GOOGLE_NEW_USER.getUserId(), PASSWORD, false, null, GOOGLE, userDetail);

    //when & then
    assertThatThrownBy(() -> sut.sendVerificationCodeToDraftUser(user))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("set for Basic Authentication");
  }

  @Test
  @DisplayName("throw if user is already active")
  void throwIfUserIsAlreadyActive() {
    //given
    UserDetails userDetail = new UserDetails(null, null, USER_EMAIL, null, DRAFT_USER);
    UserWithPassword user = new UserWithPassword(GOOGLE_NEW_USER.getUserId(), PASSWORD, true, null, BASIC_AUTH, userDetail);

    //when & then
    assertThatThrownBy(() -> sut.sendVerificationCodeToDraftUser(user))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("active");
  }

  UserVerificationCode activationCode() {
    return new UserVerificationCode(VALID_CODE_ID, draftUser(), true, CODE, CodeType.REGISTRATION, now(clock).plusHours(2));
  }

  UserVerificationCode invalidCode() {
    return new UserVerificationCode(INVALID_CODE_ID, draftUser(), true, CODE, CodeType.REGISTRATION, now(clock).minusHours(1));
  }

  UserVerificationCode resetPasswordCode() {
    return new UserVerificationCode(VALID_RESET_PASSWORD_CODE_ID, activeUser(), true, CODE, CodeType.RESET_PASSWORD, now(clock).plusHours(3));
  }

  UserVerificationCode outdatedResetPasswordCode() {
    return new UserVerificationCode(INVALID_RESET_PASSWORD_CODE_ID, activeUser(), true, CODE, CodeType.RESET_PASSWORD, now(clock).minusMonths(1));
  }

  private UserActivationProperties activationProperties() {
    return new UserActivationProperties(EXPIRES_AFTER, "REGISTRATION_SUBJECT");
  }
  private PasswordResetProperties resetProperties() {
    return new PasswordResetProperties(EXPIRES_AFTER, "RESET_PASSWORD_SUBJECT");
  }

  private UserWithPassword draftUser() {
    UserDetails userDetail = new UserDetails(null, null, USER_EMAIL, null, DRAFT_USER);
    return new UserWithPassword(GOOGLE_NEW_USER.getUserId(), PASSWORD, false, null, BASIC_AUTH, userDetail);
  }

  private UserWithPassword activeUser() {
    UserDetails userDetail = new UserDetails(null, null, USER_EMAIL, null, FREE_USER);
    return new UserWithPassword(GOOGLE_NEW_USER.getUserId(), PASSWORD, true, null, BASIC_AUTH, userDetail);
  }
}