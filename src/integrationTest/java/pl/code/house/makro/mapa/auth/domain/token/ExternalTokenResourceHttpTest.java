package pl.code.house.makro.mapa.auth.domain.token;

import static io.restassured.module.mockmvc.RestAssuredMockMvc.given;
import static org.hamcrest.Matchers.containsStringIgnoringCase;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.notNullValue;
import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.OK;
import static org.springframework.http.HttpStatus.UNAUTHORIZED;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.jdbc.JdbcTestUtils.countRowsInTable;
import static org.springframework.test.jdbc.JdbcTestUtils.countRowsInTableWhere;
import static pl.code.house.makro.mapa.auth.ApiConstraints.EXTERNAL_AUTH_BASE_PATH;
import static pl.code.house.makro.mapa.auth.domain.user.TestUser.APPLE_NEW_USER;
import static pl.code.house.makro.mapa.auth.domain.user.TestUser.FACEBOOK_NEW_USER;
import static pl.code.house.makro.mapa.auth.domain.user.TestUser.GOOGLE_NEW_USER;
import static pl.code.house.makro.mapa.auth.domain.user.TestUser.GOOGLE_PREMIUM_USER;

import io.restassured.http.ContentType;
import io.restassured.module.mockmvc.RestAssuredMockMvc;
import java.util.UUID;
import org.assertj.core.api.IntegerAssert;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;

@SpringBootTest
class ExternalTokenResourceHttpTest {

  @Autowired
  private WebApplicationContext context;

  @Autowired
  private JdbcTemplate jdbcTemplate;

  @BeforeEach
  void setup() {
    RestAssuredMockMvc.webAppContextSetup(context, springSecurity());
  }

  @Test
  @Transactional
  @DisplayName("return access token when requested by new user ")
  void returnAccessTokenWhenRequestedByNewUser() {
    //given
    assertUserCount().isEqualTo(6);
    assertUserCountByExternalId(GOOGLE_NEW_USER.getExternalId()).isEqualTo(0);

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
    ;
    assertUserCount().isEqualTo(7);
    assertUserCountByExternalId(GOOGLE_NEW_USER.getExternalId()).isEqualTo(1);

    assertAccessTokenCount().isEqualTo(2);
  }

  @Test
  @Transactional
  @DisplayName("return access token when requesting with AppleId token")
  void returnAccessTokenWhenRequestingWithAppleIdToken() {
    //given
    assertUserCount().isEqualTo(6);
    assertUserCountByExternalId(APPLE_NEW_USER.getExternalId()).isEqualTo(0);

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
    ;
    assertUserCount().isEqualTo(7);
    assertUserCountByExternalId(APPLE_NEW_USER.getExternalId()).isEqualTo(1);

    assertAccessTokenCount().isEqualTo(2);
  }

  @Test
  @Transactional
  @DisplayName("return access token when requesting with FaceBook AccessCode")
  void returnAccessTokenWhenRequestingWithFaceBookAccessCode() {
    //given
    assertUserCount().isEqualTo(6);
    assertUserCountByExternalId(FACEBOOK_NEW_USER.getExternalId()).isEqualTo(0);

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
    ;
    assertUserCount().isEqualTo(7);
    assertUserCountByExternalId(FACEBOOK_NEW_USER.getExternalId()).isEqualTo(1);

    assertAccessTokenCount().isEqualTo(2);
  }


  @Test
  @Transactional
  @DisplayName("should map jwt token to existing user and create new access token")
  void shouldMapJwtTokenToExistingUserAndCreateNewAccessToken() {
    //given
    assertAccessTokenCount().isEqualTo(1);
    assertUserCountByExternalId(GOOGLE_PREMIUM_USER.getExternalId()).isEqualTo(1);

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
    ;

    assertAccessTokenCount().isEqualTo(1);
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

  private IntegerAssert assertAccessTokenCount() {
    return new IntegerAssert(countRowsInTable(jdbcTemplate, "oauth_access_token"));
  }


  private IntegerAssert assertUserCount() {
    return new IntegerAssert(countRowsInTable(jdbcTemplate, "app_user"));
  }

  private IntegerAssert assertUserCountByExternalId(String externalId) {
    int userCount = countRowsInTableWhere(jdbcTemplate, "app_user", String.format("external_id = '%s'", externalId));
    return new IntegerAssert(userCount);
  }

}