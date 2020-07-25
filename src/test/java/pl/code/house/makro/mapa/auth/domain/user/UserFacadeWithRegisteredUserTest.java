package pl.code.house.makro.mapa.auth.domain.user;

import static java.util.Optional.of;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.only;
import static org.mockito.Mockito.times;
import static pl.code.house.makro.mapa.auth.domain.user.OAuth2Provider.BASIC_AUTH;
import static pl.code.house.makro.mapa.auth.domain.user.TestUser.NEW_REG_USER;
import static pl.code.house.makro.mapa.auth.domain.user.TestUser.REG_DRAFT_USER;
import static pl.code.house.makro.mapa.auth.domain.user.UserType.DRAFT_USER;
import static pl.code.house.makro.mapa.auth.domain.user.UserType.FREE_USER;

import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import pl.code.house.makro.mapa.auth.domain.user.TestUser.PasswordMockUser;
import pl.code.house.makro.mapa.auth.domain.user.dto.ActivationCodeDto;
import pl.code.house.makro.mapa.auth.domain.user.dto.NewUserRequest;
import pl.code.house.makro.mapa.auth.domain.user.dto.UserDto;
import pl.code.house.makro.mapa.auth.error.UserAlreadyExistsException;
import pl.code.house.makro.mapa.auth.error.UserRegistrationException;

@ExtendWith(MockitoExtension.class)
class UserFacadeWithRegisteredUserTest {

  public static final UUID DRAFT_USER_ID = UUID.randomUUID();
  private static final UUID ACTIVATION_CODE_ID = UUID.randomUUID();
  private final String code = "ACTIVATION_CODE";

  @Mock
  private UserRepository repository;

  @Mock
  private TermsAndConditionsRepository termsRepository;

  @Spy
  private PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

  @Mock
  private DraftActivationCodeService activationCodeService;

  @Mock
  private UserAuthoritiesService userAuthoritiesService;

  @InjectMocks
  private UserFacade sut;

  @Test
  @DisplayName("should correctly register new user")
  void shouldCorrectlyRegisterNewUser() {
    //given
    NewUserRequest request = new NewUserRequest("token", "clientId", NEW_REG_USER.getName(), NEW_REG_USER.getPassword());
    given(repository.findUserWithPasswordByUserEmail(NEW_REG_USER.getName())).willReturn(Optional.empty());
    given(repository.saveAndFlush(any(UserWithPassword.class))).willReturn(savedDraft(NEW_REG_USER));

    //when
    sut.registerNewUser(request);

    //then
    then(repository).should(times(1)).saveAndFlush(any(UserWithPassword.class));

    ArgumentCaptor<UserWithPassword> draftUserCaptor = ArgumentCaptor.forClass(UserWithPassword.class);
    then(activationCodeService).should(only()).sendActivationCodeToDraftUser(draftUserCaptor.capture());

    assertThat(draftUserCaptor.getValue().getId()).isEqualTo(DRAFT_USER_ID);
    assertThat(draftUserCaptor.getValue().getProvider()).isEqualTo(BASIC_AUTH);
    assertThat(draftUserCaptor.getValue().getPassword()).isEqualTo(NEW_REG_USER.getPassword());
    assertThat(draftUserCaptor.getValue().getTermsAndConditionsId()).isNull();
    assertThat(draftUserCaptor.getValue().getUserDetails().getEmail()).isEqualTo(NEW_REG_USER.getName());
    assertThat(draftUserCaptor.getValue().getUserDetails().getType()).isEqualTo(DRAFT_USER);
  }

  @Test
  @DisplayName("should send new activation code for draft user if the old code is no longer valid")
  void shouldSendNewActivationCodeForDraftUserIfTheOldCodeIsNoLongerValid() {
    //given
    NewUserRequest request = new NewUserRequest("token", "clientId", REG_DRAFT_USER.getName(), REG_DRAFT_USER.getPassword());
    given(repository.findUserWithPasswordByUserEmail(REG_DRAFT_USER.getName())).willReturn(of(savedUser(REG_DRAFT_USER.getUserId(), REG_DRAFT_USER, DRAFT_USER, false)));

    given(activationCodeService.findActivationCode(REG_DRAFT_USER.getUserId())).willReturn(Optional.empty());

    //when
    sut.registerNewUser(request);

    //then
    then(repository).should(times(0)).saveAndFlush(any(UserWithPassword.class));

    ArgumentCaptor<UserWithPassword> draftUserCaptor = ArgumentCaptor.forClass(UserWithPassword.class);
    then(activationCodeService).should(times(1)).sendActivationCodeToDraftUser(draftUserCaptor.capture());

    assertThat(draftUserCaptor.getValue().getId()).isEqualTo(REG_DRAFT_USER.getUserId());
    assertThat(draftUserCaptor.getValue().getProvider()).isEqualTo(BASIC_AUTH);
    assertThat(draftUserCaptor.getValue().getPassword()).isEqualTo(REG_DRAFT_USER.getPassword());
    assertThat(draftUserCaptor.getValue().getTermsAndConditionsId()).isNull();
    assertThat(draftUserCaptor.getValue().getUserDetails().getEmail()).isEqualTo(REG_DRAFT_USER.getName());
    assertThat(draftUserCaptor.getValue().getUserDetails().getType()).isEqualTo(DRAFT_USER);
  }

  @Test
  @DisplayName("throw if user with email already exists")
  void throwIfUserWithEmailAlreadyExists() {
    //given
    NewUserRequest request = new NewUserRequest("token", "clientId", NEW_REG_USER.getName(), NEW_REG_USER.getPassword());
    given(repository.findUserWithPasswordByUserEmail(NEW_REG_USER.getName())).willReturn(of(savedUser(NEW_REG_USER.getUserId(), NEW_REG_USER, FREE_USER, true)));

    //when & then
    assertThatThrownBy(() -> sut.registerNewUser(request))
        .isInstanceOf(UserAlreadyExistsException.class)
        .hasMessageContaining("email already exists")
    ;
  }

  @Test
  @DisplayName("throw if user already exists and is disabled")
  void throwIfUserAlreadyExistsAndIsDisabled() {
    //given
    NewUserRequest request = new NewUserRequest("token", "clientId", NEW_REG_USER.getName(), NEW_REG_USER.getPassword());
    given(repository.findUserWithPasswordByUserEmail(NEW_REG_USER.getName())).willReturn(of(savedUser(NEW_REG_USER.getUserId(), NEW_REG_USER, FREE_USER, false)));

    //when & then
    assertThatThrownBy(() -> sut.registerNewUser(request))
        .isInstanceOf(UserAlreadyExistsException.class)
        .hasMessageContaining("email already exists")
    ;
  }

  @Test
  @DisplayName("throw if registering user when draft already exists and activation code is valid")
  void throwIfRegisteringUserWhenDraftAlreadyExistsAndActivationCodeIsValid() {
    //given
    NewUserRequest request = new NewUserRequest("token", "clientId", REG_DRAFT_USER.getName(), REG_DRAFT_USER.getPassword());
    given(repository.findUserWithPasswordByUserEmail(REG_DRAFT_USER.getName())).willReturn(of(savedUser(REG_DRAFT_USER.getUserId(), REG_DRAFT_USER, DRAFT_USER, false)));

    given(activationCodeService.findActivationCode(REG_DRAFT_USER.getUserId())).willReturn(Optional.of(validCode()));

    //when
    assertThatThrownBy(() -> sut.registerNewUser(request))
        .isInstanceOf(UserAlreadyExistsException.class)
        .hasMessageContaining("has valid activation_code");
  }

  @Test
  @DisplayName("should correctly activate draft user with activation_code")
  void shouldCorrectlyActivateDraftUserWithActivationCode() {
    //given
    String clientId = "client_id";

    given(activationCodeService.findActivationCode(code)).willReturn(validCode());
    given(repository.findById(DRAFT_USER_ID)).willReturn(of(savedDraft(NEW_REG_USER)));

    //when
    UserDto userDto = sut.activateDraftBy(code, clientId);

    //then
    assertThat(userDto.getId()).isEqualTo(DRAFT_USER_ID);
    assertThat(userDto.getEnabled()).isTrue();
    assertThat(userDto.getUserDetails().getType()).isEqualTo(FREE_USER);

    then(activationCodeService).should(times(1)).useCode(ACTIVATION_CODE_ID);
  }

  @Test
  @DisplayName("throw if user assigned to activation_code is not a draft")
  void throwIfUserAssignedToActivationCodeIsNotADraft() {
    //given
    String clientId = "client_id";

    given(activationCodeService.findActivationCode(code)).willReturn(validCode());
    given(repository.findById(DRAFT_USER_ID)).willReturn(of(savedUser(NEW_REG_USER.getUserId(), NEW_REG_USER, FREE_USER, false)));

    //when
    assertThatThrownBy(() -> sut.activateDraftBy(code, clientId))
        .isInstanceOf(UserRegistrationException.class)
        .hasMessageContaining("DRAFT user")
        .hasMessageContaining("was not found")
    ;
  }

  @Test
  @DisplayName("throw if user assigned to activation_code is enabled")
  void throwIfUserAssignedToActivationCodeIsEnabled() {
    //given
    String clientId = "client_id";

    given(activationCodeService.findActivationCode(code)).willReturn(validCode());
    given(repository.findById(DRAFT_USER_ID)).willReturn(of(savedUser(NEW_REG_USER.getUserId(), NEW_REG_USER, DRAFT_USER, true)));

    //when
    assertThatThrownBy(() -> sut.activateDraftBy(code, clientId))
        .isInstanceOf(UserRegistrationException.class)
        .hasMessageContaining("Disabled")
        .hasMessageContaining("user")
        .hasMessageContaining("was not found")
    ;
  }

  private ActivationCodeDto validCode() {
    return ActivationCodeDto.builder()
        .id(ACTIVATION_CODE_ID)
        .code(code)
        .enabled(true)
        .draftUser(savedDraft(NEW_REG_USER).toDto())
        .build();
  }

  private UserWithPassword savedDraft(PasswordMockUser user) {
    return savedUser(DRAFT_USER_ID, user, DRAFT_USER, false);
  }

  private UserWithPassword savedUser(UUID userId, PasswordMockUser user, UserType userType, boolean enabled) {
    UserDetails userDetails = UserDetails.builder()
        .email(user.getName())
        .type(userType)
        .build();
    return new UserWithPassword(userId, user.getPassword(), enabled, null, BASIC_AUTH, userDetails);
  }
}