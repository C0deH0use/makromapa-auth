package pl.code.house.makro.mapa.auth.domain.user;

import static io.restassured.module.mockmvc.RestAssuredMockMvc.given;
import static io.restassured.module.mockmvc.RestAssuredMockMvc.webAppContextSetup;
import static java.nio.charset.StandardCharsets.UTF_8;
import static java.time.ZonedDateTime.now;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsStringIgnoringCase;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.nullValue;
import static org.springframework.http.HttpHeaders.AUTHORIZATION;
import static org.springframework.http.HttpHeaders.encodeBasicAuth;
import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.CREATED;
import static org.springframework.http.HttpStatus.OK;
import static org.springframework.http.HttpStatus.PRECONDITION_FAILED;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static pl.code.house.makro.mapa.auth.ApiConstraints.BASE_PATH;
import static pl.code.house.makro.mapa.auth.domain.user.TestUser.NEW_REG_USER;
import static pl.code.house.makro.mapa.auth.domain.user.TestUser.REG_DRAFT_USER;
import static pl.code.house.makro.mapa.auth.domain.user.TestUser.REG_DRAFT_USER_WITH_DISABLED_CODE;
import static pl.code.house.makro.mapa.auth.domain.user.TestUser.REG_DRAFT_USER_WITH_EXPIRED_CODE;
import static pl.code.house.makro.mapa.auth.domain.user.UserType.DRAFT_USER;
import static pl.code.house.makro.mapa.auth.domain.user.UserType.FREE_USER;

import com.icegreen.greenmail.util.GreenMail;
import io.restassured.http.Header;
import java.time.Clock;
import java.util.Optional;
import java.util.UUID;
import javax.mail.Message.RecipientType;
import javax.mail.MessagingException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;

@SpringBootTest
class UserRegistrationResourceHttpTest {

  @Autowired
  private WebApplicationContext context;

  @Autowired
  private UserRepository userRepository;

  @Autowired
  private UserAuthoritiesService authoritiesManager;

  @Autowired
  private UserActivationCodeRepository codeRepository;

  @Autowired
  private Clock clock;

  @Value("${user.activation.code.expiresOn.hours}")
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
        .param("client_id", "makromapa-mobile")
        .param("username", NEW_REG_USER.getName())
        .param("password", NEW_REG_USER.getPassword())
        .contentType(APPLICATION_JSON_VALUE)
        .header(new Header(AUTHORIZATION, "Basic " + encodeBasicAuth("makromapa-mobile", "secret", UTF_8)))

        .when()
        .post(BASE_PATH + "/user-registration")

        .then()
        .log().all()
        .status(CREATED)
        .body("communicationTarget", equalTo(NEW_REG_USER.getName()))
        .body("communicationChannel", equalTo("EMAIL"))
        .extract()
        .body()
        .jsonPath()
        .getUUID("userId");

    assertThat(codeRepository.findByUserId(newDraftId)).isPresent();

    assertThat(greenMail.getReceivedMessages()).hasSize(1);
    assertThat(greenMail.getReceivedMessages()[0].getRecipients(RecipientType.TO)[0].toString()).isEqualTo(NEW_REG_USER.getName());
  }

  @Test
  @Transactional
  @DisplayName("request activation_code for existing draft user if existing code is invalid - disabled")
  void requestActivationCodeForExistingDraftUserIfExistingCodeIsInvalidDisabled() throws MessagingException {
    //given
    Optional<UserActivationCode> existingActivationCode = codeRepository.findByUserId(REG_DRAFT_USER_WITH_DISABLED_CODE.getUserId());
    assertThat(existingActivationCode).isPresent();
    assertThat(existingActivationCode.filter(UserActivationCode::getEnabled)).isEmpty();

    given()
        .param("grant_type", "external-token")
        .param("client_id", "makromapa-mobile")
        .param("username", REG_DRAFT_USER_WITH_DISABLED_CODE.getName())
        .param("password", REG_DRAFT_USER_WITH_DISABLED_CODE.getPassword())
        .contentType(APPLICATION_JSON_VALUE)
        .header(new Header(AUTHORIZATION, "Basic " + encodeBasicAuth("makromapa-mobile", "secret", UTF_8)))

        .when()
        .post(BASE_PATH + "/user-registration")

        .then()
        .log().all()
        .status(CREATED)
        .body("userId", equalTo(REG_DRAFT_USER_WITH_DISABLED_CODE.getUserId().toString()))
        .body("communicationTarget", equalTo(REG_DRAFT_USER_WITH_DISABLED_CODE.getName()))
        .body("communicationChannel", equalTo("EMAIL"))
    ;

    Optional<UserActivationCode> newActivationCode = codeRepository.findByUserId(REG_DRAFT_USER_WITH_DISABLED_CODE.getUserId());
    assertThat(newActivationCode).isPresent();
    assertThat(newActivationCode.get().getEnabled()).isTrue();
    assertThat(newActivationCode.get().getExpiresOn()).isBeforeOrEqualTo(now(clock).plusHours(expiresOn));

    assertThat(greenMail.getReceivedMessages()).hasSize(1);
    assertThat(greenMail.getReceivedMessages()[0].getRecipients(RecipientType.TO)[0].toString()).isEqualTo(REG_DRAFT_USER_WITH_DISABLED_CODE.getName());
  }

  @Test
  @Transactional
  @DisplayName("request activation_code for existing draft user if existing code is invalid - has expired")
  void requestActivationCodeForExistingDraftUserIfExistingCodeIsInvalidHasExpired() throws MessagingException {
    //given
    assertThat(codeRepository.findByUserId(REG_DRAFT_USER_WITH_EXPIRED_CODE.getUserId()).filter(ac -> ac.getExpiresOn().isBefore(now(clock)))).isPresent();

    given()
        .param("grant_type", "external-token")
        .param("client_id", "makromapa-mobile")
        .param("username", REG_DRAFT_USER_WITH_EXPIRED_CODE.getName())
        .param("password", REG_DRAFT_USER_WITH_EXPIRED_CODE.getPassword())
        .contentType(APPLICATION_JSON_VALUE)
        .header(new Header(AUTHORIZATION, "Basic " + encodeBasicAuth("makromapa-mobile", "secret", UTF_8)))

        .when()
        .post(BASE_PATH + "/user-registration")

        .then()
        .log().all()
        .status(CREATED)
        .body("userId", equalTo(REG_DRAFT_USER_WITH_EXPIRED_CODE.getUserId().toString()))
        .body("communicationTarget", equalTo(REG_DRAFT_USER_WITH_EXPIRED_CODE.getName()))
        .body("communicationChannel", equalTo("EMAIL"))
    ;

    Optional<UserActivationCode> newActivationCode = codeRepository.findByUserId(REG_DRAFT_USER_WITH_EXPIRED_CODE.getUserId());
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

    assertThat(codeRepository.findByUserId(REG_DRAFT_USER.getUserId()).filter(ac -> ac.getExpiresOn().isAfter(now(clock)))).isPresent();

    assertThat(authoritiesManager.userAuthorities(REG_DRAFT_USER.getUserId())).hasSize(0);

    given()
        .contentType(APPLICATION_JSON_VALUE)
        .header(new Header(AUTHORIZATION, "Basic " + encodeBasicAuth("makromapa-mobile", "secret", UTF_8)))

        .when()
        .post(BASE_PATH + "/user-registration/activate/{code}", REG_DRAFT_USER.getActivationCode())

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
  @DisplayName("Return BAD_REQUEST when registering user that already has been registered and activation_code is valid")
  void returnBadRequestWhenRegisteringUserThatAlreadyHasBeenRegisteredAndActivationCodeIsValid() {
    assertThat(codeRepository.findByUserId(REG_DRAFT_USER.getUserId())
        .filter(UserActivationCode::getEnabled)
        .filter(ac -> ac.getExpiresOn().isAfter(now(clock)))
    ).isPresent();

    given()
        .param("grant_type", "external-token")
        .param("client_id", "makromapa-mobile")
        .param("username", REG_DRAFT_USER.getName())
        .param("password", REG_DRAFT_USER.getPassword())
        .contentType(APPLICATION_JSON_VALUE)
        .header(new Header(AUTHORIZATION, "Basic " + encodeBasicAuth("makromapa-mobile", "secret", UTF_8)))

        .when()
        .post(BASE_PATH + "/user-registration")

        .then()
        .log().all()
        .status(BAD_REQUEST)
        .body("error", containsStringIgnoringCase("has valid activation_code"))
    ;
  }

  @Test
  @DisplayName("Return PRECONDITION_FAILED when activating user with invalid code - expired")
  void returnPreconditionFailedWhenActivatingUserWithInvalidCodeExpired() {
    given()
        .contentType(APPLICATION_JSON_VALUE)
        .header(new Header(AUTHORIZATION, "Basic " + encodeBasicAuth("makromapa-mobile", "secret", UTF_8)))

        .when()
        .post(BASE_PATH + "/user-registration/activate/{code}", REG_DRAFT_USER_WITH_EXPIRED_CODE.getActivationCode())

        .then()
        .log().all()
        .status(PRECONDITION_FAILED)
        .body("error", containsStringIgnoringCase("not find any VALID activationCode"))
    ;
  }

  @Test
  @DisplayName("Return PRECONDITION_FAILED when activating user with invalid code - disabled")
  void returnPreconditionFailedWhenActivatingUserWithInvalidCodeDisabled() {
    given()
        .contentType(APPLICATION_JSON_VALUE)
        .header(new Header(AUTHORIZATION, "Basic " + encodeBasicAuth("makromapa-mobile", "secret", UTF_8)))

        .when()
        .post(BASE_PATH + "/user-registration/activate/{code}", REG_DRAFT_USER_WITH_DISABLED_CODE.getActivationCode())

        .then()
        .log().all()
        .status(PRECONDITION_FAILED)
        .body("error", containsStringIgnoringCase("not find any VALID activationCode"))
    ;
  }
}