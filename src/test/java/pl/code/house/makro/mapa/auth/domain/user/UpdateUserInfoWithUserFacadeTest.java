package pl.code.house.makro.mapa.auth.domain.user;

import static java.util.Optional.empty;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.AdditionalAnswers.returnsFirstArg;
import static org.mockito.Answers.RETURNS_SELF;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static pl.code.house.makro.mapa.auth.domain.user.OAuth2Provider.BASIC_AUTH;
import static pl.code.house.makro.mapa.auth.domain.user.OAuth2Provider.GOOGLE;
import static pl.code.house.makro.mapa.auth.domain.user.PremiumFeature.PREMIUM;
import static pl.code.house.makro.mapa.auth.domain.user.TestUser.GOOGLE_NEW_USER;
import static pl.code.house.makro.mapa.auth.domain.user.TestUser.GOOGLE_PREMIUM_USER;
import static pl.code.house.makro.mapa.auth.domain.user.TestUser.REG_USER;
import static pl.code.house.makro.mapa.auth.domain.user.UserType.FREE_USER;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.AdditionalAnswers;
import org.mockito.Answers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import pl.code.house.makro.mapa.auth.domain.user.dto.UserInfoDto;
import pl.code.house.makro.mapa.auth.domain.user.dto.UserInfoUpdateDto;
import pl.code.house.makro.mapa.auth.error.UserNotExistsException;

@ExtendWith(MockitoExtension.class)
class UpdateUserInfoWithUserFacadeTest {

  @Mock
  private UserRepository repository;

  @Mock
  private UserAuthoritiesService userAuthoritiesService;

  @InjectMocks
  private UserFacade sut;

  @Test
  @DisplayName("update user info for existing premium user")
  void updateUserInfoForExistingPremiumUser() {
    //given
    UUID userId = GOOGLE_PREMIUM_USER.getUserId();
    UserDetails details = UserDetails.builder()
        .type(FREE_USER)
        .email("old@google.com")
        .name(GOOGLE_NEW_USER.getName())
        .points(500)
        .build();
    ExternalUser premiumUser = new ExternalUser(
        userId,
        1000L,
        GOOGLE,
        details,
        GOOGLE_PREMIUM_USER.getExternalId(),
        true
    );
    UserInfoUpdateDto updatedDetails = new UserInfoUpdateDto("NEW NAME", "NEW SURNAME", "NEW NICKNAME", "PICTURE");

    given(repository.findById(userId)).willReturn(Optional.of(premiumUser));
    given(repository.save(any(BaseUser.class))).willAnswer(returnsFirstArg());
    given(userAuthoritiesService.getUserAuthorities(userId)).willReturn(List.of(new SimpleGrantedAuthority("ROLE_PREMIUM")));

    //when
    UserInfoDto infoDto = sut.updateUserInfo(userId, updatedDetails);

    //then
    assertThat(infoDto.getSub()).isEqualTo(userId);
    assertThat(infoDto.getEnabled()).isEqualTo(true);
    assertThat(infoDto.getProvider()).isEqualTo(GOOGLE);
    assertThat(infoDto.getType()).isEqualTo(FREE_USER);
    assertThat(infoDto.getPremiumFeatures()).contains(PREMIUM);
    assertThat(infoDto.getEmail()).isEqualTo("old@google.com");

    assertThat(infoDto.getName()).isEqualTo("NEW NAME");
    assertThat(infoDto.getSurname()).isEqualTo("NEW SURNAME");
    assertThat(infoDto.getSurname()).isEqualTo("NEW SURNAME");
    assertThat(infoDto.getPicture()).isEqualTo("PICTURE");
    assertThat(infoDto.getPoints()).isEqualTo(500);
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
    UserInfoUpdateDto updatedDetails = new UserInfoUpdateDto("NEW NAME", "NEW SURNAME", "Nickname", "PICTURE");

    given(repository.findById(userId)).willReturn(Optional.of(user));
    given(repository.save(any(BaseUser.class))).willAnswer(returnsFirstArg());
    given(userAuthoritiesService.getUserAuthorities(userId)).willReturn(List.of());

    //when
    UserInfoDto infoDto = sut.updateUserInfo(userId, updatedDetails);

    //then
    assertThat(infoDto.getSub()).isEqualTo(userId);
    assertThat(infoDto.getEnabled()).isEqualTo(true);
    assertThat(infoDto.getProvider()).isEqualTo(BASIC_AUTH);
    assertThat(infoDto.getType()).isEqualTo(FREE_USER);
    assertThat(infoDto.getPremiumFeatures()).isEmpty();
    assertThat(infoDto.getEmail()).isEqualTo(REG_USER.getName());
    assertThat(infoDto.getName()).isEqualTo("NEW NAME");
    assertThat(infoDto.getSurname()).isEqualTo("NEW SURNAME");
    assertThat(infoDto.getNickname()).isEqualTo("Nickname");
    assertThat(infoDto.getPicture()).isEqualTo("PICTURE");
    assertThat(infoDto.getPoints()).isEqualTo(0);
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
        .points(1000)
        .build();
    UserWithPassword user = new UserWithPassword(userId, "", true, null, details);
    UserInfoUpdateDto updatedDetails = new UserInfoUpdateDto("NEW NAME", "", "NICK", null);

    given(repository.findById(userId)).willReturn(Optional.of(user));
    given(repository.save(any(BaseUser.class))).willAnswer(returnsFirstArg());
    given(userAuthoritiesService.getUserAuthorities(userId)).willReturn(List.of());

    //when
    UserInfoDto infoDto = sut.updateUserInfo(userId, updatedDetails);

    //then
    assertThat(infoDto.getSub()).isEqualTo(userId);
    assertThat(infoDto.getEnabled()).isEqualTo(true);
    assertThat(infoDto.getProvider()).isEqualTo(BASIC_AUTH);
    assertThat(infoDto.getType()).isEqualTo(FREE_USER);
    assertThat(infoDto.getPremiumFeatures()).isEmpty();
    assertThat(infoDto.getEmail()).isEqualTo(REG_USER.getName());

    assertThat(infoDto.getName()).isEqualTo("NEW NAME");
    assertThat(infoDto.getSurname()).isEmpty();
    assertThat(infoDto.getNickname()).isEqualTo("NICK");
    assertThat(infoDto.getPicture()).isEqualTo(expectedPic);
    assertThat(infoDto.getPoints()).isEqualTo(1000);
  }

  @Test
  @DisplayName("throw if updating user info of non existing user")
  void throwIfUpdatingUserInfoOfNonExistingUser() {
    //given
    UUID userId = REG_USER.getUserId();
    UserInfoUpdateDto updatedDetails = new UserInfoUpdateDto("NEW NAME", "SURNAME", "", "PICTURE");
    given(repository.findById(userId)).willReturn(empty());

    //when
    assertThatThrownBy(() -> sut.updateUserInfo(userId, updatedDetails))
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
    UserInfoUpdateDto updatedDetails = new UserInfoUpdateDto("NEW NAME", "NEW SURNAME", null, "PICTURE");

    given(repository.findById(userId)).willReturn(Optional.of(user));

    //when
    assertThatThrownBy(() -> sut.updateUserInfo(userId, updatedDetails))
        .isInstanceOf(UserNotExistsException.class)
        .hasMessageContaining("User")
        .hasMessageContaining(userId.toString())
        .hasMessageContaining("does not exists")
    ;
  }
}
