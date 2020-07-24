package pl.code.house.makro.mapa.auth.domain.user;

import static java.time.ZoneOffset.UTC;
import static java.util.Optional.empty;
import static java.util.Optional.ofNullable;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.times;
import static pl.code.house.makro.mapa.auth.domain.mail.EmailType.REGISTRATION;
import static pl.code.house.makro.mapa.auth.domain.user.CommunicationProtocol.EMAIL;
import static pl.code.house.makro.mapa.auth.domain.user.OAuth2Provider.BASIC_AUTH;
import static pl.code.house.makro.mapa.auth.domain.user.OAuth2Provider.GOOGLE;
import static pl.code.house.makro.mapa.auth.domain.user.TestUser.GOOGLE_NEW_USER;
import static pl.code.house.makro.mapa.auth.domain.user.UserType.DRAFT_USER;
import static pl.code.house.makro.mapa.auth.domain.user.UserType.FREE_USER;

import java.time.Clock;
import java.time.Instant;
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
import pl.code.house.makro.mapa.auth.domain.user.dto.ActivationLinkDto;

@ExtendWith(MockitoExtension.class)
class DraftActivationCodeServiceTest {

  private static final String PASSWORD = "DEFAULT_PASWD";
  private static final String USER_EMAIL = "email@domain.com";

  private final UserActivationProperties properties = properties();

  private final Clock clock = Clock.fixed(Instant.parse("2020-07-03T10:15:30.00Z"), UTC);

  @Mock
  private UserActivationCodeRepository repository;

  @Mock
  private EmailService emailService;

  private DraftActivationCodeService sut;

  @BeforeEach
  void setUp() {
    sut = new DraftActivationCodeService(clock, properties, repository, emailService);
  }

  @Test
  @DisplayName("should create and send email with valid draft user")
  void shouldCreateAndSendEmailWithValidDraftUser() {
    //given
    UserWithPassword user = draftUser();
    given(repository.findActiveCodeByUserId(GOOGLE_NEW_USER.getUserId())).willReturn(empty());

    //when
    ActivationLinkDto linkDto = sut.sendActivationCodeToDraftUser(user);

    //then
    ArgumentCaptor<MessageDetails> messageCapture = ArgumentCaptor.forClass(MessageDetails.class);

    then(emailService).should(times(1)).sendHtmlMail(messageCapture.capture());
    MessageDetails captureValue = messageCapture.getValue();
    assertThat(captureValue.getType()).isEqualTo(REGISTRATION);
    assertThat(captureValue.getReceiver()).isEqualTo(USER_EMAIL);
    assertThat(captureValue.getContext().getVariable("activation_code")).isNotNull();

    assertThat(linkDto.getActivationLink()).isNotBlank();
    assertThat(linkDto.getCommunicationChannel()).isEqualTo(EMAIL);
    assertThat(linkDto.getCommunicationTarget()).isEqualTo(USER_EMAIL);
  }

  @Test
  @DisplayName("throw if user is not a draft user")
  void throwIfUserIsNotADraftUser() {
    //given
    UserDetails userDetail = new UserDetails(null, null, USER_EMAIL, null, FREE_USER);
    UserWithPassword user = new UserWithPassword(GOOGLE_NEW_USER.getUserId(), PASSWORD, false, null, null, BASIC_AUTH, userDetail);

    //when & then
    assertThatThrownBy(() -> sut.sendActivationCodeToDraftUser(user))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("must be of DRAFT");
  }

  @Test
  @DisplayName("throw if user does not have email")
  void throwIfUserDoesNotHaveEmail() {
    //given
    UserDetails userDetail = new UserDetails(null, null, " ", null, DRAFT_USER);
    UserWithPassword user = new UserWithPassword(GOOGLE_NEW_USER.getUserId(), PASSWORD, false, null, null, BASIC_AUTH, userDetail);

    //when & then
    assertThatThrownBy(() -> sut.sendActivationCodeToDraftUser(user))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("must have a username");
  }

  @Test
  @DisplayName("throw if user does not have a password stored")
  void throwIfUserDoesNotHaveAPasswordStored() {
    //given
    UserDetails userDetail = new UserDetails(null, null, USER_EMAIL, null, DRAFT_USER);
    UserWithPassword user = new UserWithPassword(GOOGLE_NEW_USER.getUserId(), null, false, null, null, BASIC_AUTH, userDetail);

    //when & then
    assertThatThrownBy(() -> sut.sendActivationCodeToDraftUser(user))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("have password");
  }

  @Test
  @DisplayName("throw if user is not set for BASIC_AUTH")
  void throwIfUserIsNotSetForBasicAuth() {
    //given
    UserDetails userDetail = new UserDetails(null, null, USER_EMAIL, null, DRAFT_USER);
    UserWithPassword user = new UserWithPassword(GOOGLE_NEW_USER.getUserId(), PASSWORD, false, null, null, GOOGLE, userDetail);

    //when & then
    assertThatThrownBy(() -> sut.sendActivationCodeToDraftUser(user))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("set for Basic Authentication");
  }

  @Test
  @DisplayName("throw if user is already active")
  void throwIfUserIsAlreadyActive() {
    //given
    UserDetails userDetail = new UserDetails(null, null, USER_EMAIL, null, DRAFT_USER);
    UserWithPassword user = new UserWithPassword(GOOGLE_NEW_USER.getUserId(), PASSWORD, true, null, null, BASIC_AUTH, userDetail);

    //when & then
    assertThatThrownBy(() -> sut.sendActivationCodeToDraftUser(user))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("active");
  }

  @Test
  @DisplayName("throw if user has an activation code already assinged to him")
  void throwIfUserHasAnActivationCodeAlreadyAssingedToHim() {
    //given
    UserDetails userDetail = new UserDetails(null, null, USER_EMAIL, null, DRAFT_USER);
    UserWithPassword user = new UserWithPassword(GOOGLE_NEW_USER.getUserId(), PASSWORD, false, null, null, BASIC_AUTH, userDetail);

    given(repository.findActiveCodeByUserId(GOOGLE_NEW_USER.getUserId())).willReturn(ofNullable(activationCode()));

    //when & then
    assertThatThrownBy(() -> sut.sendActivationCodeToDraftUser(user))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("active activation_code registered");
  }

  UserActivationCode activationCode() {
    return new UserActivationCode(UUID.randomUUID(), null, true, UUID.randomUUID().toString(), null);
  }

  private UserActivationProperties properties() {
    return new UserActivationProperties(10, "REGISTRATION_SUBJECT");
  }

  private UserWithPassword draftUser() {
    UserDetails userDetail = new UserDetails(null, null, USER_EMAIL, null, DRAFT_USER);
    return new UserWithPassword(GOOGLE_NEW_USER.getUserId(), PASSWORD, false, null, null, BASIC_AUTH, userDetail);
  }
}