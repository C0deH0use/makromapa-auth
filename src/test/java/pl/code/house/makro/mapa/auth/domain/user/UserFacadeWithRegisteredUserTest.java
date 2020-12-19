package pl.code.house.makro.mapa.auth.domain.user;

import static java.util.Optional.empty;
import static java.util.Optional.of;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.only;
import static org.mockito.Mockito.times;
import static pl.code.house.makro.mapa.auth.domain.user.CodeType.REGISTRATION;
import static pl.code.house.makro.mapa.auth.domain.user.CodeType.RESET_PASSWORD;
import static pl.code.house.makro.mapa.auth.domain.user.OAuth2Provider.BASIC_AUTH;
import static pl.code.house.makro.mapa.auth.domain.user.TestUser.NEW_REG_USER;
import static pl.code.house.makro.mapa.auth.domain.user.TestUser.REG_DRAFT_USER;
import static pl.code.house.makro.mapa.auth.domain.user.TestUser.REG_USER;
import static pl.code.house.makro.mapa.auth.domain.user.UserType.DRAFT_USER;
import static pl.code.house.makro.mapa.auth.domain.user.UserType.FREE_USER;
import static pl.code.house.makro.mapa.auth.error.UserOperationError.DRAFT_NOT_FOUND;

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
import pl.code.house.makro.mapa.auth.domain.user.dto.ActivateUserRequest;
import pl.code.house.makro.mapa.auth.domain.user.dto.NewUserRequest;
import pl.code.house.makro.mapa.auth.domain.user.dto.UserDto;
import pl.code.house.makro.mapa.auth.domain.user.dto.VerificationCodeDto;
import pl.code.house.makro.mapa.auth.error.PasswordResetException;
import pl.code.house.makro.mapa.auth.error.UserAlreadyExistsException;
import pl.code.house.makro.mapa.auth.error.UserNotExistsException;
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
  private VerificationCodeService activationCodeService;

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
    then(activationCodeService).should(only()).sendVerificationCodeToDraftUser(draftUserCaptor.capture());

    assertThat(draftUserCaptor.getValue().getId()).isEqualTo(DRAFT_USER_ID);
    assertThat(draftUserCaptor.getValue().getProvider()).isEqualTo(BASIC_AUTH);
    assertThat(draftUserCaptor.getValue().getPassword()).isEqualTo(NEW_REG_USER.getPassword());
    assertThat(draftUserCaptor.getValue().getTermsAndConditionsId()).isNull();
    assertThat(draftUserCaptor.getValue().getUserDetails().getEmail()).isEqualTo(NEW_REG_USER.getName());
    assertThat(draftUserCaptor.getValue().getUserDetails().getType()).isEqualTo(DRAFT_USER);
  }

  @Test
  @DisplayName("should send new activation code for draft user if the old code is no longer valid")
  void shouldSendNewVerificationCodeForDraftUserIfTheOldCodeIsNoLongerValid() {
    //given
    NewUserRequest request = new NewUserRequest("token", "clientId", REG_DRAFT_USER.getName(), REG_DRAFT_USER.getPassword());
    given(repository.findUserWithPasswordByUserEmail(REG_DRAFT_USER.getName())).willReturn(of(savedUser(REG_DRAFT_USER.getUserId(), REG_DRAFT_USER, DRAFT_USER, false)));

    given(activationCodeService.findVerificationCode(REG_DRAFT_USER.getUserId(), REGISTRATION)).willReturn(Optional.empty());

    //when
    sut.registerNewUser(request);

    //then
    then(repository).should(times(0)).saveAndFlush(any(UserWithPassword.class));

    ArgumentCaptor<UserWithPassword> draftUserCaptor = ArgumentCaptor.forClass(UserWithPassword.class);
    then(activationCodeService).should(times(1)).sendVerificationCodeToDraftUser(draftUserCaptor.capture());

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
  void throwIfRegisteringUserWhenDraftAlreadyExistsAndVerificationCodeIsValid() {
    //given
    NewUserRequest request = new NewUserRequest("token", "clientId", REG_DRAFT_USER.getName(), REG_DRAFT_USER.getPassword());
    given(repository.findUserWithPasswordByUserEmail(REG_DRAFT_USER.getName())).willReturn(of(savedUser(REG_DRAFT_USER.getUserId(), REG_DRAFT_USER, DRAFT_USER, false)));

    given(activationCodeService.findVerificationCode(REG_DRAFT_USER.getUserId(), REGISTRATION)).willReturn(Optional.of(validCode()));

    //when
    assertThatThrownBy(() -> sut.registerNewUser(request))
        .isInstanceOf(UserAlreadyExistsException.class)
        .hasMessageContaining("has valid verification_code");
  }

  @Test
  @DisplayName("should correctly activate draft user with verification_code")
  void shouldCorrectlyActivateDraftUserWithVerificationCode() {
    //given
    String clientId = "client_id";
    ActivateUserRequest request = new ActivateUserRequest(NEW_REG_USER.getName(), code);

    given(activationCodeService.findVerificationCode(code, REGISTRATION)).willReturn(validCode());
    given(repository.findUserWithPasswordByUserEmail(NEW_REG_USER.getName())).willReturn(of(savedDraft(NEW_REG_USER)));

    //when
    UserDto userDto = sut.activateDraftBy(request, clientId);

    //then
    assertThat(userDto.getId()).isEqualTo(DRAFT_USER_ID);
    assertThat(userDto.getEnabled()).isTrue();
    assertThat(userDto.getUserDetails().getType()).isEqualTo(FREE_USER);

    then(activationCodeService).should(times(1)).useCode(ACTIVATION_CODE_ID);
  }

  @Test
  @DisplayName("should correctly issue a reset password verification_code")
  void shouldCorrectlyIssueAResetPasswordVerificationCode() {
      //given
    String clientId = "client_id";
    String userEmail = REG_USER.getName();
    UserWithPassword savedUser = savedUser();

    given(repository.findUserWithPasswordByUserEmail(userEmail)).willReturn(of(savedUser));

    //when
    sut.resetPasswordFor(userEmail);

    //then
    ArgumentCaptor<UserWithPassword> user = ArgumentCaptor.forClass(UserWithPassword.class);

    then(activationCodeService).should(times(1)).sendResetPasswordToActiveUser(user.capture());

    assertThat(user.getValue().getId()).isEqualTo(REG_USER.getUserId());
    assertThat(user.getValue().getProvider()).isEqualTo(BASIC_AUTH);
    assertThat(user.getValue().getPassword()).isEqualTo(REG_USER.getPassword());
    assertThat(user.getValue().getTermsAndConditionsId()).isNull();
    assertThat(user.getValue().getUserDetails().getEmail()).isEqualTo(REG_USER.getName());
    assertThat(user.getValue().getUserDetails().getType()).isEqualTo(FREE_USER);
  }

  @Test
  @DisplayName("should correctly change password for user with valid verification_code")
  void shouldCorrectlyChangePasswordForUserWithValidVerificationCode() {
    //given
    String newPassword = "NEW_PASSWORD";
    UserWithPassword savedUser = savedUser();
    given(activationCodeService.findVerificationCode(code, RESET_PASSWORD)).willReturn(validResetPasswordCode());
    given(repository.findUserWithPasswordByUserEmail(REG_USER.getName())).willReturn(of(savedUser));

    //when
    sut.changeUserPassword(REG_USER.getName(), code, newPassword);

    //then
    ArgumentCaptor<String> newPasswordCaptor = ArgumentCaptor.forClass(String.class);
    then(activationCodeService).should(times(1)).useCode(ACTIVATION_CODE_ID);
    then(repository).should(times(1)).updateUserPassword(eq(REG_USER.getUserId()), newPasswordCaptor.capture());

    assertThat(passwordEncoder.matches(newPassword, newPasswordCaptor.getValue())).isTrue();
  }

  @Test
  @DisplayName("throw if user assigned to verification_code is not a draft")
  void throwIfUserAssignedToVerificationCodeIsNotADraft() {
    //given
    String clientId = "client_id";
    ActivateUserRequest request = new ActivateUserRequest(NEW_REG_USER.getName(), code);

    given(activationCodeService.findVerificationCode(code, REGISTRATION)).willReturn(validCode());
    given(repository.findUserWithPasswordByUserEmail(NEW_REG_USER.getName())).willReturn(of(savedUser(NEW_REG_USER.getUserId(), NEW_REG_USER, FREE_USER, false)));

    //when
    assertThatThrownBy(() -> sut.activateDraftBy(request, clientId))
        .isInstanceOf(UserRegistrationException.class)
        .hasMessage("Could not find any eligible draft user with the following email")
        .hasFieldOrPropertyWithValue("error", DRAFT_NOT_FOUND)
    ;
  }

  @Test
  @DisplayName("throw if using code assigned to different user")
  void throwIfUsingCodeAssignedToDifferentUser() {
      //given
    String clientId = "client_id";
    ActivateUserRequest request = new ActivateUserRequest(NEW_REG_USER.getName(), code);

    given(activationCodeService.findVerificationCode(code, REGISTRATION)).willReturn(validCode());
    UserWithPassword savedUser = savedUser(UUID.randomUUID(), NEW_REG_USER, DRAFT_USER, false);
    given(repository.findUserWithPasswordByUserEmail(NEW_REG_USER.getName())).willReturn(of(savedUser));

    //when
    assertThatThrownBy(() -> sut.activateDraftBy(request, clientId))
        .isInstanceOf(UserRegistrationException.class)
        .hasMessageContaining("Validation Code is assigned to different user")
        ;
  }

  @Test
  @DisplayName("throw if user assigned to verification_code is enabled")
  void throwIfUserAssignedToVerificationCodeIsEnabled() {
    //given
    String clientId = "client_id";
    ActivateUserRequest request = new ActivateUserRequest(NEW_REG_USER.getName(), code);

    given(activationCodeService.findVerificationCode(code, REGISTRATION)).willReturn(validCode());
    given(repository.findUserWithPasswordByUserEmail(NEW_REG_USER.getName())).willReturn(of(savedUser(NEW_REG_USER.getUserId(), NEW_REG_USER, DRAFT_USER, true)));

    //when
    assertThatThrownBy(() -> sut.activateDraftBy(request, clientId))
        .isInstanceOf(UserRegistrationException.class)
        .hasMessage("Could not find any eligible draft user with the following email")
        .hasFieldOrPropertyWithValue("error", DRAFT_NOT_FOUND)
    ;
  }

  @Test
  @DisplayName("throw if user passed for reset password does not exist")
  void throwIfUserPassedForResetPasswordDoesNotExist() {
    //given
    String userEmail = REG_USER.getName();

    given(repository.findUserWithPasswordByUserEmail(userEmail)).willReturn(empty());

    //when
    assertThatThrownBy( () -> sut.resetPasswordFor(userEmail))
        .isInstanceOf(UserNotExistsException.class)
        .hasMessage("Could not find user by email: " + userEmail);
  }

  @Test
  @DisplayName("throw if user passed for reset password is a draft")
  void throwIfUserPassedForResetPasswordIsADraft() {
    //given
    String userEmail = REG_USER.getName();
    UserWithPassword savedUser = savedUser(REG_USER.getUserId(), REG_USER, DRAFT_USER, false);

    given(repository.findUserWithPasswordByUserEmail(userEmail)).willReturn(of(savedUser));

    //when
    assertThatThrownBy( () -> sut.resetPasswordFor(userEmail))
        .isInstanceOf(PasswordResetException.class)
        .hasMessage("Cannot request reset password. User must be an active one");
  }

  @Test
  @DisplayName("throw if requesting password with code assigned to different user")
  void throwIfRequestingPasswordWithCodeAssignedToDifferentUser() {
    //given
    String newPassword = "NEW_PASSWORD";
    UserWithPassword savedUser = savedUser(UUID.randomUUID(), NEW_REG_USER, FREE_USER, true);
    given(activationCodeService.findVerificationCode(code, RESET_PASSWORD)).willReturn(validResetPasswordCode());
    given(repository.findUserWithPasswordByUserEmail(NEW_REG_USER.getName())).willReturn(of(savedUser));

    //when
    assertThatThrownBy(() -> sut.changeUserPassword(NEW_REG_USER.getName(), code, newPassword))
        .isInstanceOf(PasswordResetException.class)
        .hasMessageContaining("Validation Code is assigned to different user")
    ;
  }

  private VerificationCodeDto validCode() {
    return VerificationCodeDto.builder()
        .id(ACTIVATION_CODE_ID)
        .code(code)
        .enabled(true)
        .user(savedDraft(NEW_REG_USER).toDto())
        .codeType(REGISTRATION)
        .build();
  }

  private VerificationCodeDto validResetPasswordCode() {
    return VerificationCodeDto.builder()
        .id(ACTIVATION_CODE_ID)
        .code(code)
        .enabled(true)
        .user(savedUser().toDto())
        .codeType(RESET_PASSWORD)
        .build();
  }

  private UserWithPassword savedDraft(PasswordMockUser user) {
    return savedUser(DRAFT_USER_ID, user, DRAFT_USER, false);
  }

  private UserWithPassword savedUser() {
    return savedUser(REG_USER.getUserId(), REG_USER, FREE_USER, true);
  }

  private UserWithPassword savedUser(UUID userId, PasswordMockUser user, UserType userType, boolean enabled) {
    UserDetails userDetails = UserDetails.builder()
        .email(user.getName())
        .type(userType)
        .build();
    return new UserWithPassword(userId, user.getPassword(), enabled, null, userDetails);
  }
}