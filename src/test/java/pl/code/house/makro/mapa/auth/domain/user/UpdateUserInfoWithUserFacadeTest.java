package pl.code.house.makro.mapa.auth.domain.user;

import static java.util.Optional.empty;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;
import static pl.code.house.makro.mapa.auth.domain.user.OAuth2Provider.BASIC_AUTH;
import static pl.code.house.makro.mapa.auth.domain.user.OAuth2Provider.GOOGLE;
import static pl.code.house.makro.mapa.auth.domain.user.TestUser.GOOGLE_NEW_USER;
import static pl.code.house.makro.mapa.auth.domain.user.TestUser.GOOGLE_PREMIUM_USER;
import static pl.code.house.makro.mapa.auth.domain.user.TestUser.REG_USER;
import static pl.code.house.makro.mapa.auth.domain.user.UserType.FREE_USER;
import static pl.code.house.makro.mapa.auth.domain.user.UserType.PREMIUM_USER;

import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import pl.code.house.makro.mapa.auth.domain.user.dto.UserInfoDto;
import pl.code.house.makro.mapa.auth.error.UserNotExistsException;

@ExtendWith(MockitoExtension.class)
class UpdateUserInfoWithUserFacadeTest {

  @Mock
  private UserRepository repository;

  @InjectMocks
  private UserFacade sut;

  @Test
  @DisplayName("update user info for existing premium user")
  void updateUserInfoForExistingPremiumUser() {
    //given
    UUID userId = GOOGLE_PREMIUM_USER.getUserId();
    UserDetails details = UserDetails.builder()
        .type(PREMIUM_USER)
        .email("old@google.com")
        .name(GOOGLE_NEW_USER.getName())
        .build();
    ExternalUser premiumUser = new ExternalUser(
        userId,
        1000L,
        GOOGLE,
        details,
        GOOGLE_PREMIUM_USER.getExternalId(),
        true
    );
    UserDetails updatedDetails = new UserDetails("NEW NAME", "NEW SURNAME", null, "PICTURE", null);

    given(repository.findById(userId)).willReturn(Optional.of(premiumUser));

    //when
    UserInfoDto infoDto = sut.updateUserDetails(userId, updatedDetails);

    //then
    assertThat(infoDto.getSub()).isEqualTo(userId);
    assertThat(infoDto.getEnabled()).isEqualTo(true);
    assertThat(infoDto.getProvider()).isEqualTo(GOOGLE);
    assertThat(infoDto.getType()).isEqualTo(PREMIUM_USER);
    assertThat(infoDto.getEmail()).isEqualTo("old@google.com");

    assertThat(infoDto.getName()).isEqualTo("NEW NAME");
    assertThat(infoDto.getSurname()).isEqualTo("NEW SURNAME");
    assertThat(infoDto.getPicture()).isEqualTo("PICTURE");
  }

  @Test
  @DisplayName("update user info for basic auth user")
  void updateUserInfoForBasicAuthUser() {
    //given
    UUID userId = REG_USER.getUserId();
    UserDetails details = UserDetails.builder()
        .type(FREE_USER)
        .email(REG_USER.getName())
        .build();
    UserWithPassword user = new UserWithPassword(userId, "", true, null, details);
    UserDetails updatedDetails = new UserDetails("NEW NAME", "NEW SURNAME", null, "PICTURE", null);

    given(repository.findById(userId)).willReturn(Optional.of(user));

    //when
    UserInfoDto infoDto = sut.updateUserDetails(userId, updatedDetails);

    //then
    assertThat(infoDto.getSub()).isEqualTo(userId);
    assertThat(infoDto.getEnabled()).isEqualTo(true);
    assertThat(infoDto.getProvider()).isEqualTo(BASIC_AUTH);
    assertThat(infoDto.getType()).isEqualTo(FREE_USER);
    assertThat(infoDto.getEmail()).isEqualTo(REG_USER.getName());

    assertThat(infoDto.getName()).isEqualTo("NEW NAME");
    assertThat(infoDto.getSurname()).isEqualTo("NEW SURNAME");
    assertThat(infoDto.getPicture()).isEqualTo("PICTURE");
  }

  @Test
  @DisplayName("update only properties that where passed as not null")
  void updateOnlyPropertiesThatWherePassedAsNotNull() {
    //given
    UUID userId = REG_USER.getUserId();
    String expectedPic = "old_pic";
    UserDetails details = UserDetails.builder()
        .type(FREE_USER)
        .email(REG_USER.getName())
        .name("name")
        .surname("surname")
        .picture(expectedPic)
        .build();
    UserWithPassword user = new UserWithPassword(userId, "", true, null, details);
    UserDetails updatedDetails = new UserDetails("NEW NAME", "", null, null, null);

    given(repository.findById(userId)).willReturn(Optional.of(user));

    //when
    UserInfoDto infoDto = sut.updateUserDetails(userId, updatedDetails);

    //then
    assertThat(infoDto.getSub()).isEqualTo(userId);
    assertThat(infoDto.getEnabled()).isEqualTo(true);
    assertThat(infoDto.getProvider()).isEqualTo(BASIC_AUTH);
    assertThat(infoDto.getType()).isEqualTo(FREE_USER);
    assertThat(infoDto.getEmail()).isEqualTo(REG_USER.getName());

    assertThat(infoDto.getName()).isEqualTo("NEW NAME");
    assertThat(infoDto.getSurname()).isEmpty();
    assertThat(infoDto.getPicture()).isEqualTo(expectedPic);
  }

  @Test
  @DisplayName("throw if updating user info of non existing user")
  void throwIfUpdatingUserInfoOfNonExistingUser() {
    //given
    UUID userId = REG_USER.getUserId();
    UserDetails updatedDetails = new UserDetails("NEW NAME", "NEW SURNAME", null, "PICTURE", null);

    given(repository.findById(userId)).willReturn(empty());

    //when
    assertThatThrownBy(() -> sut.updateUserDetails(userId, updatedDetails))
        .isInstanceOf(UserNotExistsException.class)
        .hasMessageContaining("User")
        .hasMessageContaining(userId.toString())
        .hasMessageContaining("does not exists")
    ;
  }

  @Test
  @DisplayName("throw if updating user info for disabled user")
  void throwIfUpdatingUserInfoForDisabledUser() {
    UUID userId = REG_USER.getUserId();
    UserDetails details = UserDetails.builder()
        .type(FREE_USER)
        .email(REG_USER.getName())
        .build();
    UserWithPassword user = new UserWithPassword(userId, "", false, null, details);
    UserDetails updatedDetails = new UserDetails("NEW NAME", "NEW SURNAME", null, "PICTURE", null);

    given(repository.findById(userId)).willReturn(Optional.of(user));

    //when
    assertThatThrownBy(() -> sut.updateUserDetails(userId, updatedDetails))
        .isInstanceOf(UserNotExistsException.class)
        .hasMessageContaining("User")
        .hasMessageContaining(userId.toString())
        .hasMessageContaining("does not exists")
    ;
  }
}
