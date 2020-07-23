package pl.code.house.makro.mapa.auth.domain.user;

import static java.util.Optional.empty;
import static java.util.Optional.ofNullable;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;
import static pl.code.house.makro.mapa.auth.domain.user.CommunicationProtocol.EMAIL;
import static pl.code.house.makro.mapa.auth.domain.user.OAuth2Provider.BASIC_AUTH;
import static pl.code.house.makro.mapa.auth.domain.user.OAuth2Provider.GOOGLE;
import static pl.code.house.makro.mapa.auth.domain.user.TestUser.GOOGLE_NEW_USER;
import static pl.code.house.makro.mapa.auth.domain.user.UserType.DRAFT_USER;
import static pl.code.house.makro.mapa.auth.domain.user.UserType.FREE_USER;

import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import pl.code.house.makro.mapa.auth.domain.user.dto.ActivationLinkDto;

@ExtendWith(MockitoExtension.class)
class DraftActivationCodeServiceTest {

  private static final String PASSWORD = "DEFAULT_PASWD";
  private static final String USER_EMAIL = "email@domain.com";

  @Mock
  private UserActivationCodeRepository repository;

  @InjectMocks
  private DraftActivationCodeService sut;

  @Test
  @DisplayName("should create and send email with valid draft user")
  void shouldCreateAndSendEmailWithValidDraftUser() {
    //given
    UserWithPassword user = draftUser();
    given(repository.findActiveCodeByUserId(GOOGLE_NEW_USER.getUserId())).willReturn(empty());

    //when
    ActivationLinkDto linkDto = sut.sendActivationCodeToDraftUser(user);

    //then
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


  private UserWithPassword draftUser() {
    UserDetails userDetail = new UserDetails(null, null, USER_EMAIL, null, DRAFT_USER);
    return new UserWithPassword(GOOGLE_NEW_USER.getUserId(), PASSWORD, false, null, null, BASIC_AUTH, userDetail);
  }
}