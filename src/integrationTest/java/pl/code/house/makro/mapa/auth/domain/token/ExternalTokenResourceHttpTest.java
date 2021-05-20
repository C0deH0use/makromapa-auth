package pl.code.house.makro.mapa.auth.domain.token;

import static io.restassured.module.mockmvc.RestAssuredMockMvc.given;
import static io.restassured.module.mockmvc.RestAssuredMockMvc.webAppContextSetup;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.containsStringIgnoringCase;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.notNullValue;
import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.OK;
import static org.springframework.http.HttpStatus.UNAUTHORIZED;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static pl.code.house.makro.mapa.auth.ApiConstraints.EXTERNAL_AUTH_BASE_PATH;
import static pl.code.house.makro.mapa.auth.domain.user.TestUser.APPLE_NEW_USER;
import static pl.code.house.makro.mapa.auth.domain.user.TestUser.FACEBOOK_NEW_USER;
import static pl.code.house.makro.mapa.auth.domain.user.TestUser.GOOGLE_NEW_USER;
import static pl.code.house.makro.mapa.auth.domain.user.TestUser.GOOGLE_PREMIUM_USER;

import io.restassured.http.ContentType;
import java.util.UUID;
import org.assertj.core.api.LongAssert;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.Rollback;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;
import pl.code.house.makro.mapa.auth.domain.user.TestUserRepository;

@SpringBootTest
class ExternalTokenResourceHttpTest {

  @Autowired
  private WebApplicationContext context;

  @Autowired
  private TestOAuthAccessTokenRepository oAuthAccessTokenRepository;

  @Autowired
  private TestUserRepository userRepository;

  @BeforeEach
  void setup() {
    webAppContextSetup(context, springSecurity());
  }

  @Test
  @Transactional
  @DisplayName("return access token when requested by new user ")
  void returnAccessTokenWhenRequestedByNewUser() {
    //given
    assertUserCount().isEqualTo(7);
    assertUserCountByExternalId(GOOGLE_NEW_USER.getExternalId()).isZero();
    assertAccessTokenCount().isOne();

    given()
        .param("grant_type", "external-token")
        .param("client_id", "makromapa-mobile")
        .contentType(APPLICATION_JSON_VALUE)
        .header(GOOGLE_NEW_USER.getAuthenticationHeader())

        .when()
        .post(EXTERNAL_AUTH_BASE_PATH + "/token")

        .then()
        .log().all()
        .status(OK)

        .body("token_type", equalTo("bearer"))
        .body("access_token", notNullValue())
        .body("refresh_token", notNullValue())
        .body("expires_in", greaterThanOrEqualTo(900))
        .body("scope", equalTo("FREE_USER"))
    ;
    assertUserCount().isEqualTo(8);
    assertUserCountByExternalId(GOOGLE_NEW_USER.getExternalId()).isOne();

    assertAccessTokenCount().isEqualTo(2);
  }

  @Test
  @Transactional
  @DisplayName("return access token when requesting with AppleId token")
  void returnAccessTokenWhenRequestingWithAppleIdToken() {
    //given
    assertUserCount().isEqualTo(7);
    assertUserCountByExternalId(APPLE_NEW_USER.getExternalId()).isZero();
    assertAccessTokenCount().isOne();

    given()
        .param("grant_type", "external-token")
        .param("client_id", "makromapa-mobile")
        .contentType(APPLICATION_JSON_VALUE)
        .header(APPLE_NEW_USER.getAuthenticationHeader())

        .log().all(true)
        .when()
        .post(EXTERNAL_AUTH_BASE_PATH + "/token")

        .then()
        .log().all()
        .status(OK)

        .body("token_type", equalTo("bearer"))
        .body("access_token", notNullValue())
        .body("refresh_token", notNullValue())
        .body("expires_in", greaterThanOrEqualTo(0))
        .body("scope", equalTo("FREE_USER"))
    ;
    assertUserCount().isEqualTo(8);
    assertUserCountByExternalId(APPLE_NEW_USER.getExternalId()).isOne();

    assertAccessTokenCount().isEqualTo(2);
  }

  @Test
  @Transactional
  @DisplayName("return access token when requesting with FaceBook AccessCode")
  void returnAccessTokenWhenRequestingWithFaceBookAccessCode() {
    //given
    assertUserCount().isEqualTo(7);
    assertUserCountByExternalId(FACEBOOK_NEW_USER.getExternalId()).isZero();
    assertAccessTokenCount().isOne();

    given()
        .param("grant_type", "external-token")
        .param("client_id", "makromapa-mobile")
        .contentType(APPLICATION_JSON_VALUE)
        .header(FACEBOOK_NEW_USER.getAuthenticationHeader())

        .log().all(true)
        .when()
        .post(EXTERNAL_AUTH_BASE_PATH + "/code")

        .then()
        .log().all()
        .status(OK)

        .body("token_type", equalTo("bearer"))
        .body("access_token", notNullValue())
        .body("refresh_token", notNullValue())
        .body("expires_in", greaterThanOrEqualTo(0))
        .body("scope", equalTo("FREE_USER"))
    ;
    assertUserCount().isEqualTo(8);
    assertUserCountByExternalId(FACEBOOK_NEW_USER.getExternalId()).isOne();

    assertAccessTokenCount().isEqualTo(2);
  }

  @Test
  @Transactional
  @DisplayName("should map jwt token to existing user and create new access token")
  void shouldMapJwtTokenToExistingUserAndCreateNewAccessToken() {
    //given
    assertUserCountByExternalId(GOOGLE_PREMIUM_USER.getExternalId()).isOne();
    assertAccessTokenCount().isOne();

    given()
        .log().all()
        .param("grant_type", "external-token")
        .param("client_id", "makromapa-mobile")
        .header(GOOGLE_PREMIUM_USER.getAuthenticationHeader())
        .contentType(ContentType.JSON)

        .when()
        .post(EXTERNAL_AUTH_BASE_PATH + "/token")

        .then()
        .log().all(true)
        .status(OK)
        .body("token_type", equalTo("bearer"))
        .body("access_token", notNullValue())
        .body("refresh_token", notNullValue())
        .body("expires_in", greaterThanOrEqualTo(1))
        .body("scope", containsString("FREE_USER"))
        .body("scope", containsString("DISABLE_ADS"))
        .body("scope", containsString("PREMIUM"))
    ;

    assertAccessTokenCount().isOne();
  }

  @Test
  @DisplayName("return 400 when grant_type missing")
  void return400WhenGrantTypeMissing() {
    given()
        .param("client_id", "makromapa-mobile")
        .header(GOOGLE_PREMIUM_USER.getAuthenticationHeader())
        .contentType(ContentType.JSON)

        .when()
        .post(EXTERNAL_AUTH_BASE_PATH + "/token")

        .then()
        .log().ifValidationFails()
        .status(BAD_REQUEST)
        .body("uniqueErrorId", notNullValue(UUID.class))
        .body("error", containsStringIgnoringCase("Missing grant type"))
    ;
  }

  @Test
  @DisplayName("return 401 when client_id missing")
  void return401WhenClientIdMissing() {
    given()
        .param("grant_type", "external-token")
        .header(GOOGLE_PREMIUM_USER.getAuthenticationHeader())
        .contentType(ContentType.JSON)

        .when()
        .post(EXTERNAL_AUTH_BASE_PATH + "/token")

        .then()
        .log().ifValidationFails()
        .status(UNAUTHORIZED)
        .body("uniqueErrorId", notNullValue(UUID.class))
        .body("error", containsStringIgnoringCase("Missing client authentication Id"))
    ;
  }

  private LongAssert assertAccessTokenCount() {
    return new LongAssert(oAuthAccessTokenRepository.count());
  }

  private LongAssert assertUserCount() {
    return new LongAssert(userRepository.count());
  }

  private LongAssert assertUserCountByExternalId(String externalId) {
    return new LongAssert(userRepository.countByExternalId(externalId));
  }

}