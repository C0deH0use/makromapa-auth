package pl.code.house.makro.mapa.auth.domain.user;

import static io.restassured.module.mockmvc.RestAssuredMockMvc.given;
import static io.restassured.module.mockmvc.RestAssuredMockMvc.webAppContextSetup;
import static java.nio.charset.StandardCharsets.UTF_8;
import static java.time.ZonedDateTime.now;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.containsStringIgnoringCase;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.nullValue;
import static org.springframework.http.HttpHeaders.AUTHORIZATION;
import static org.springframework.http.HttpHeaders.encodeBasicAuth;
import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.CREATED;
import static org.springframework.http.HttpStatus.NOT_FOUND;
import static org.springframework.http.HttpStatus.OK;
import static org.springframework.http.HttpStatus.PRECONDITION_FAILED;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static pl.code.house.makro.mapa.auth.ApiConstraints.BASE_PATH;
import static pl.code.house.makro.mapa.auth.domain.user.CodeType.REGISTRATION;
import static pl.code.house.makro.mapa.auth.domain.user.CodeType.RESET_PASSWORD;
import static pl.code.house.makro.mapa.auth.domain.user.TestUser.NEW_REG_USER;
import static pl.code.house.makro.mapa.auth.domain.user.TestUser.REG_DRAFT_USER;
import static pl.code.house.makro.mapa.auth.domain.user.TestUser.REG_DRAFT_USER_WITH_DISABLED_CODE;
import static pl.code.house.makro.mapa.auth.domain.user.TestUser.REG_DRAFT_USER_WITH_EXPIRED_CODE;
import static pl.code.house.makro.mapa.auth.domain.user.TestUser.REG_USER;
import static pl.code.house.makro.mapa.auth.domain.user.TestUser.REG_USER_2;
import static pl.code.house.makro.mapa.auth.domain.user.UserType.DRAFT_USER;
import static pl.code.house.makro.mapa.auth.domain.user.UserType.FREE_USER;

import com.icegreen.greenmail.util.GreenMail;
import io.restassured.http.Header;
import java.time.Clock;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import javax.mail.Message.RecipientType;
import javax.mail.MessagingException;
import lombok.SneakyThrows;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;

@SpringBootTest
class UserResourceHttpTest {

  private static final String CODE = "123456";
  private static final UUID EXISTING_CODE_ID = UUID.fromString("8a8aea25-467e-4fb9-99c2-ce30c442a373");

  @Autowired
  private WebApplicationContext context;

  @Autowired
  private PasswordEncoder passwordEncoder;

  @Autowired
  private UserRepository userRepository;

  @Autowired
  private UserAuthoritiesService authoritiesManager;

  @Autowired
  private UserVerificationCodeRepository codeRepository;

  @Autowired
  private Clock clock;

  @Value("${mails.registration.verification.code.expiresOn.hours}")
  private Long expiresOn;

  private GreenMail greenMail;

  @BeforeEach
  void setup() {
    greenMail = new GreenMail();
    greenMail.setUser("user_greenMain", "secret_password");
    greenMail.start();

    webAppContextSetup(context, springSecurity());
  }

  @AfterEach
  void stop() {
    greenMail.stop();
  }

  @Test
  @Transactional
  @DisplayName("register new user by mobile client")
  void registerNewUserByMobileClient() throws MessagingException {
    //given
    UUID newDraftId = given()
        .param("grant_type", "external-token")
        .param("client_id", "basic-auth-makromapa-mobile")
        .param("username", NEW_REG_USER.getName())
        .param("password", NEW_REG_USER.getPassword())
        .contentType(APPLICATION_JSON_VALUE)
        .header(new Header(AUTHORIZATION, "Basic " + encodeBasicAuth("basic-auth-makromapa-mobile", "secret", UTF_8)))

        .when()
        .post(BASE_PATH + "/user/registration")

        .then()
        .log().all()
        .status(CREATED)
        .body("codeType", equalTo("REGISTRATION"))
        .body("communicationTarget", equalTo(NEW_REG_USER.getName()))
        .body("communicationChannel", equalTo("EMAIL"))
        .extract()
        .body()
        .jsonPath()
        .getUUID("userId");

    assertThat(codeRepository.findByUserIdAndCodeType(newDraftId, REGISTRATION)).isNotEmpty();

    assertThat(greenMail.getReceivedMessages()).hasSize(1);
    assertThat(greenMail.getReceivedMessages()[0].getRecipients(RecipientType.TO)[0].toString()).isEqualTo(NEW_REG_USER.getName());
  }

  @Test
  @Transactional
  @DisplayName("request activation_code for existing draft user if existing code is invalid - disabled")
  void requestActivationCodeForExistingDraftUserIfExistingCodeIsInvalidDisabled() throws MessagingException {
    //given
    Optional<UserVerificationCode> existingActivationCode = codeRepository.findByUserIdAndCodeType(REG_DRAFT_USER_WITH_DISABLED_CODE.getUserId(), REGISTRATION).stream().findFirst();
    assertThat(existingActivationCode).isPresent();
    assertThat(existingActivationCode.filter(UserVerificationCode::getEnabled)).isEmpty();

    given()
        .param("grant_type", "external-token")
        .param("client_id", "basic-auth-makromapa-mobile")
        .param("username", REG_DRAFT_USER_WITH_DISABLED_CODE.getName())
        .param("password", REG_DRAFT_USER_WITH_DISABLED_CODE.getPassword())
        .contentType(APPLICATION_JSON_VALUE)
        .header(new Header(AUTHORIZATION, "Basic " + encodeBasicAuth("basic-auth-makromapa-mobile", "secret", UTF_8)))

        .when()
        .post(BASE_PATH + "/user/registration")

        .then()
        .log().all()
        .status(CREATED)
        .body("codeType", equalTo("REGISTRATION"))
        .body("communicationChannel", equalTo("EMAIL"))
        .body("communicationTarget", equalTo(REG_DRAFT_USER_WITH_DISABLED_CODE.getName()))
        .body("userId", equalTo(REG_DRAFT_USER_WITH_DISABLED_CODE.getUserId().toString()))
    ;

    List<UserVerificationCode> newActivationCode = codeRepository.findByUserIdAndCodeType(REG_DRAFT_USER_WITH_DISABLED_CODE.getUserId(), REGISTRATION);
    assertThat(newActivationCode).hasSize(2);
    assertThat(newActivationCode.stream().anyMatch(UserVerificationCode::getEnabled)).isTrue();
    assertThat(newActivationCode.stream().map(UserVerificationCode::getExpiresOn).anyMatch(exp -> exp.isBefore(now(clock).plusHours(expiresOn)))).isTrue();

    assertThat(greenMail.getReceivedMessages()).hasSize(1);
    assertThat(greenMail.getReceivedMessages()[0].getRecipients(RecipientType.TO)[0].toString()).isEqualTo(REG_DRAFT_USER_WITH_DISABLED_CODE.getName());
  }

  @Test
  @Transactional
  @DisplayName("request activation_code for existing draft user if existing code is invalid - has expired")
  void requestActivationCodeForExistingDraftUserIfExistingCodeIsInvalidHasExpired() throws MessagingException {
    //given
    assertThat(codeRepository.findByUserIdAndCodeType(REG_DRAFT_USER_WITH_EXPIRED_CODE.getUserId(), REGISTRATION).stream().anyMatch(ac -> ac.getExpiresOn().isBefore(now(clock)))).isTrue();

    given()
        .param("grant_type", "external-token")
        .param("client_id", "basic-auth-makromapa-mobile")
        .param("username", REG_DRAFT_USER_WITH_EXPIRED_CODE.getName())
        .param("password", REG_DRAFT_USER_WITH_EXPIRED_CODE.getPassword())
        .contentType(APPLICATION_JSON_VALUE)
        .header(new Header(AUTHORIZATION, "Basic " + encodeBasicAuth("basic-auth-makromapa-mobile", "secret", UTF_8)))

        .when()
        .post(BASE_PATH + "/user/registration")

        .then()
        .log().all()
        .status(CREATED)
        .body("codeType", equalTo("REGISTRATION"))
        .body("communicationChannel", equalTo("EMAIL"))
        .body("communicationTarget", equalTo(REG_DRAFT_USER_WITH_EXPIRED_CODE.getName()))
        .body("userId", equalTo(REG_DRAFT_USER_WITH_EXPIRED_CODE.getUserId().toString()))

    ;

    Optional<UserVerificationCode> newActivationCode = codeRepository.findByUserIdAndCodeType(REG_DRAFT_USER_WITH_EXPIRED_CODE.getUserId(), REGISTRATION)
        .stream()
        .findFirst();
    assertThat(newActivationCode).isPresent();
    assertThat(newActivationCode.get().getEnabled()).isTrue();
    assertThat(newActivationCode.get().getExpiresOn()).isBeforeOrEqualTo(now(clock).plusHours(expiresOn));

    assertThat(greenMail.getReceivedMessages()).hasSize(1);
    assertThat(greenMail.getReceivedMessages()[0].getRecipients(RecipientType.TO)[0].toString()).isEqualTo(REG_DRAFT_USER_WITH_EXPIRED_CODE.getName());
  }

  @Test
  @Transactional
  @DisplayName("activate draft user")
  void activateDraftUser() {
    //given
    BaseUser user = userRepository.findById(REG_DRAFT_USER.getUserId()).orElseThrow();
    assertThat(user.getEnabled()).isFalse();
    assertThat(user.getUserDetails().getType()).isEqualTo(DRAFT_USER);

    assertThat(codeRepository.findByUserIdAndCodeType(REG_DRAFT_USER.getUserId(), REGISTRATION).stream().anyMatch(ac -> ac.getExpiresOn().isAfter(now(clock)))).isTrue();

    assertThat(authoritiesManager.userAuthorities(REG_DRAFT_USER.getUserId())).hasSize(0);

    given()
        .contentType(APPLICATION_JSON_VALUE)
        .header(new Header(AUTHORIZATION, "Basic " + encodeBasicAuth("basic-auth-makromapa-mobile", "secret", UTF_8)))
        .param("email", REG_DRAFT_USER.getName())
        .param("verificationCode", REG_DRAFT_USER.getActivationCode())

        .when()
        .post(BASE_PATH + "/user/activate")

        .then()
        .log().all()
        .status(OK)
        .body("id", equalTo(REG_DRAFT_USER.getUserId().toString()))
        .body("externalId", nullValue())
        .body("provider", equalTo("BASIC_AUTH"))
        .body("enabled", equalTo(true))
        .body("userDetails.name", nullValue())
        .body("userDetails.surname", nullValue())
        .body("userDetails.picture", nullValue())
        .body("userDetails.email", equalTo(REG_DRAFT_USER.getName()))
        .body("userDetails.type", equalTo("FREE_USER"))
    ;

    //then
    user = userRepository.findById(REG_DRAFT_USER.getUserId()).orElseThrow();
    assertThat(user.getEnabled()).isTrue();
    assertThat(user.getUserDetails().getType()).isEqualTo(FREE_USER);

    assertThat(authoritiesManager.userAuthorities(REG_DRAFT_USER.getUserId())).hasSize(1);
  }

  @Test
  @Transactional
  @DisplayName("should request user password reset")
  void shouldRequestUserPasswordReset() {
    //given
    BaseUser user = userRepository.findById(REG_USER_2.getUserId()).orElseThrow();
    assertThat(user.getEnabled()).isTrue();
    assertThat(user.getUserDetails().getType()).isEqualTo(FREE_USER);

    assertThat(codeRepository.findByUserIdAndCodeType(REG_USER_2.getUserId(), RESET_PASSWORD)).isEmpty();

    //when
    given()
        .contentType(APPLICATION_JSON_VALUE)
        .header(new Header(AUTHORIZATION, "Basic " + encodeBasicAuth("basic-auth-makromapa-mobile", "secret", UTF_8)))
        .param("email", user.getUserDetails().getEmail())

        .when()
        .post(BASE_PATH + "/user/password/reset")

        .then()
        .log().all()
        .status(OK)
        .body("codeType", equalTo("RESET_PASSWORD"))
        .body("communicationChannel", equalTo("EMAIL"))
        .body("communicationTarget", equalTo(REG_USER_2.getName()))
        .body("userId", equalTo(REG_USER_2.getUserId().toString()))
    ;

    assertThat(codeRepository.findByUserIdAndCodeType(REG_USER_2.getUserId(), RESET_PASSWORD)).hasSize(1);
  }

  @Test
  @Transactional
  @DisplayName("request user password reset when user already has one request active")
  void requestUserPasswordResetWhenUserAlreadyHasOneRequestActive() {
    //given

    BaseUser user = userRepository.findById(REG_USER.getUserId()).orElseThrow();

    assertThat(user.getEnabled()).isTrue();
    assertThat(user.getUserDetails().getType()).isEqualTo(FREE_USER);

    assertThat(codeRepository.findById(EXISTING_CODE_ID)).isNotEmpty();
    assertThat(codeRepository.findByUserIdAndCodeType(REG_USER.getUserId(), RESET_PASSWORD)).hasSize(1);

    //when
    given()
        .contentType(APPLICATION_JSON_VALUE)
        .header(new Header(AUTHORIZATION, "Basic " + encodeBasicAuth("basic-auth-makromapa-mobile", "secret", UTF_8)))
        .param("email", user.getUserDetails().getEmail())

        .when()
        .post(BASE_PATH + "/user/password/reset")

        .then()
        .log().all()
        .status(OK)
        .body("codeType", equalTo("RESET_PASSWORD"))
        .body("communicationChannel", equalTo("EMAIL"))
        .body("communicationTarget", equalTo(REG_USER.getName()))
        .body("userId", equalTo(REG_USER.getUserId().toString()))
    ;

    //then
    assertThat(codeRepository.findById(EXISTING_CODE_ID)).isEmpty();
    assertThat(codeRepository.findByUserIdAndCodeType(REG_USER.getUserId(), RESET_PASSWORD)).hasSize(1);
    assertThat(codeRepository.findByUserIdAndCodeType(REG_USER.getUserId(), RESET_PASSWORD).get(0).getCode()).isNotEqualTo(CODE);
  }

  @Test
  @Transactional
  @DisplayName("should change user password with verification code")
  void shouldChangeUserPasswordWithVerificationCode() {
    //given
    String newPassword = "NEW_PaSSW0RD";
    given()
        .contentType(APPLICATION_JSON_VALUE)
        .header(new Header(AUTHORIZATION, "Basic " + encodeBasicAuth("basic-auth-makromapa-mobile", "secret", UTF_8)))
        .param("code", CODE)
        .param("email", REG_USER.getName())
        .param("newPassword", newPassword)

        .when()
        .post(BASE_PATH + "/user/password/change")

        .then()
        .log().all()
        .status(OK)
    ;

    UserWithPassword user = (UserWithPassword) userRepository.findById(REG_USER.getUserId()).orElseThrow();
    assertThat(passwordEncoder.matches(newPassword, user.getPassword())).isTrue();
    assertThat(codeRepository.findById(EXISTING_CODE_ID)).get().extracting(UserVerificationCode::getEnabled).isEqualTo(false);
  }

  @Test
  @DisplayName("return PRECONDITION_FAILED when updating password with verification code assigned to different user")
  void returnPreconditionFailedWhenUpdatingPasswordWithVerificationCodeAssignedToDifferentUser() {
    //given
    String newPassword = "NEW_PaSSW0RD";
    given()
        .contentType(APPLICATION_JSON_VALUE)
        .header(new Header(AUTHORIZATION, "Basic " + encodeBasicAuth("basic-auth-makromapa-mobile", "secret", UTF_8)))
        .param("code", CODE)
        .param("email", REG_USER_2.getName())
        .param("newPassword", newPassword)

        .when()
        .post(BASE_PATH + "/user/password/change")

        .then()
        .log().all()
        .status(PRECONDITION_FAILED)
        .body("error", containsString("assigned to different user"))
    ;
  }

  @Test
  @DisplayName("return PRECONDITION_FAILED if requesting password reset for draft user")
  void returnPreconditionFailedIfRequestingPasswordResetForDraftUser() {
    //given
    given()
        .contentType(APPLICATION_JSON_VALUE)
        .header(new Header(AUTHORIZATION, "Basic " + encodeBasicAuth("basic-auth-makromapa-mobile", "secret", UTF_8)))
        .param("email", REG_DRAFT_USER.getName())

        .when()
        .post(BASE_PATH + "/user/password/reset")

        .then()
        .log().all()
        .status(PRECONDITION_FAILED)
        .body("error", containsString("Cannot request reset password. User must be an active one"))
    ;
  }

  @Test
  @DisplayName("return NOT_FOUND when requesting password reset for unknown user")
  void returnNotFoundWhenRequestingPasswordResetForUnknownUser() {
    //given
    String UNKNOWN_USER = "unknown@mail.com";

    given()
        .contentType(APPLICATION_JSON_VALUE)
        .header(new Header(AUTHORIZATION, "Basic " + encodeBasicAuth("basic-auth-makromapa-mobile", "secret", UTF_8)))
        .param("email", UNKNOWN_USER)

        .when()
        .post(BASE_PATH + "/user/password/reset")

        .then()
        .log().all()
        .status(NOT_FOUND)
        .body("error", equalTo("Could not find user by email: " + UNKNOWN_USER))
    ;
  }

  @Test
  @DisplayName("Return BAD_REQUEST when registering user that already has been registered and activation_code is valid")
  void returnBadRequestWhenRegisteringUserThatAlreadyHasBeenRegisteredAndActivationCodeIsValid() {
    assertThat(codeRepository.findByUserIdAndCodeType(REG_DRAFT_USER.getUserId(), REGISTRATION)
        .stream()
        .filter(UserVerificationCode::getEnabled)
        .anyMatch(ac -> ac.getExpiresOn().isAfter(now(clock)))
    ).isTrue();

    given()
        .param("grant_type", "external-token")
        .param("client_id", "basic-auth-makromapa-mobile")
        .param("username", REG_DRAFT_USER.getName())
        .param("password", REG_DRAFT_USER.getPassword())
        .contentType(APPLICATION_JSON_VALUE)
        .header(new Header(AUTHORIZATION, "Basic " + encodeBasicAuth("basic-auth-makromapa-mobile", "secret", UTF_8)))

        .when()
        .post(BASE_PATH + "/user/registration")

        .then()
        .log().all()
        .status(BAD_REQUEST)
        .body("error", containsStringIgnoringCase("has valid verification_code"))
    ;
  }

  @Test
  @DisplayName("Return PRECONDITION_FAILED when activating user with invalid code - expired")
  void returnPreconditionFailedWhenActivatingUserWithInvalidCodeExpired() {
    given()
        .contentType(APPLICATION_JSON_VALUE)
        .header(new Header(AUTHORIZATION, "Basic " + encodeBasicAuth("basic-auth-makromapa-mobile", "secret", UTF_8)))

        .param("email",  REG_DRAFT_USER_WITH_DISABLED_CODE.getName())
        .param("verificationCode",  REG_DRAFT_USER_WITH_DISABLED_CODE.getActivationCode())

        .when()
        .post(BASE_PATH + "/user/activate")

        .then()
        .log().all()
        .status(PRECONDITION_FAILED)
        .body("error", containsStringIgnoringCase("not find any VALID verificationCode"))
    ;
  }

  @Test
  @DisplayName("Return PRECONDITION_FAILED when activating user with invalid code - disabled")
  void returnPreconditionFailedWhenActivatingUserWithInvalidCodeDisabled() {
    given()
        .contentType(APPLICATION_JSON_VALUE)
        .header(new Header(AUTHORIZATION, "Basic " + encodeBasicAuth("basic-auth-makromapa-mobile", "secret", UTF_8)))

        .param("email",  REG_DRAFT_USER_WITH_DISABLED_CODE.getName())
        .param("verificationCode",  REG_DRAFT_USER_WITH_DISABLED_CODE.getActivationCode())

        .when()
        .post(BASE_PATH + "/user/activate")

        .then()
        .log().all()
        .status(PRECONDITION_FAILED)
        .body("error", containsStringIgnoringCase("not find any VALID verificationCode"))
    ;
  }


}