package pl.code.house.makro.mapa.auth.domain.token;

import static io.restassured.module.mockmvc.RestAssuredMockMvc.given;
import static java.nio.charset.StandardCharsets.UTF_8;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasItems;
import static org.hamcrest.Matchers.lessThanOrEqualTo;
import static org.hamcrest.Matchers.notNullValue;
import static org.springframework.http.HttpHeaders.encodeBasicAuth;
import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.FORBIDDEN;
import static org.springframework.http.HttpStatus.OK;
import static org.springframework.http.HttpStatus.UNAUTHORIZED;
import static org.springframework.http.MediaType.APPLICATION_FORM_URLENCODED_VALUE;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static pl.code.house.makro.mapa.auth.ApiConstraints.EXTERNAL_AUTH_BASE_PATH;
import static pl.code.house.makro.mapa.auth.domain.user.TestUser.GOOGLE_PREMIUM_USER;

import io.restassured.http.ContentType;
import io.restassured.http.Header;
import io.restassured.module.mockmvc.RestAssuredMockMvc;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;

@SpringBootTest
class TokenResourceHttpTest {

  private static final String ACCESS_TOKEN = "10c7fc72-64f6-4c9a-af2b-a5d33c65ecf3";

  @Autowired
  private WebApplicationContext context;

  @BeforeEach
  void setup() {
    RestAssuredMockMvc.webAppContextSetup(context, springSecurity());
  }

  @Test
  @Transactional
  @DisplayName("return OK with new token")
  void returnOkWithNewToken() {
    given()
        .param("grant_type", "password")
        .param("username", "user_1@example.com")
        .param("password", "secret")
        .header(new Header(HttpHeaders.AUTHORIZATION, "Basic " + encodeBasicAuth("makromapa-mobile", "secret", UTF_8)))
        .contentType(APPLICATION_FORM_URLENCODED_VALUE)
        .log().all(true)

        .when()
        .post("/oauth/token")

        .then()
        .log().ifValidationFails()
        .status(OK)

        .body("token_type", equalTo("bearer"))
        .body("scope", equalTo("user"))
        .body("access_token", notNullValue())
        .body("refresh_token", notNullValue())
        .body("expires_in", lessThanOrEqualTo(900))
    ;
  }

  @Test
  @Transactional
  @DisplayName("return token details when requesting with valid access token")
  void returnTokenDetailsWhenRequestingWithValidAccessToken() {
    String userAccessToken = getUserAccessToken();

    given()
        .param("token", userAccessToken)
        .header(new Header(HttpHeaders.AUTHORIZATION, "Basic " + encodeBasicAuth("makromapa-backend", "secret", UTF_8)))
        .contentType(APPLICATION_FORM_URLENCODED_VALUE)

        .when()
        .get("/oauth/check_token")

        .then()
        .log().all()
        .status(OK)
        .body("active", equalTo(true))
        .body("exp", notNullValue())
        .body("user_name", equalTo("118364847911502210416"))
        .body("client_id", equalTo("makromapa-mobile"))
        .body("scope", hasItems("PREMIUM_USER", "USER"))

    ;
  }

  @Test
  @DisplayName("return FORBIDDEN when requesting token details by an unauthorized user")
  void returnForbiddenWhenRequestingTokenDetailsByAnUnauthorizedUser() {
    given()
        .param("token", ACCESS_TOKEN)
        .header(new Header(HttpHeaders.AUTHORIZATION, "Basic " + encodeBasicAuth("makromapa-admin", "secret", UTF_8)))
        .contentType(APPLICATION_FORM_URLENCODED_VALUE)

        .when()
        .get("/oauth/check_token")

        .then()
        .log().all()
        .status(FORBIDDEN)
    ;
  }

  @Test
  @DisplayName("return BadRequest when token is with invalid token type")
  void returnBadRequestWhenTokenIsWithInvalidTokenType() {
    given()
        .param("token", "")
        .contentType(APPLICATION_FORM_URLENCODED_VALUE)
        .header(new Header(HttpHeaders.AUTHORIZATION, "Basic " + encodeBasicAuth("makromapa-backend", "secret", UTF_8)))

        .when()
        .get("/oauth/check_token")

        .then()
        .log().ifValidationFails()
        .status(BAD_REQUEST)
        .body("error", equalTo("invalid_token"))
    ;
  }

  @Test
  @DisplayName("return unauthorized when requesting token without user details")
  void returnUnauthorizedWhenRequestingTokenWithoutUserDetails() {
    given()
        .param("token", "")
        .contentType(APPLICATION_FORM_URLENCODED_VALUE)

        .when()
        .get("/oauth/check_token")

        .then()
        .log().ifValidationFails()
        .status(UNAUTHORIZED)
    ;
  }

  @Test
  @DisplayName("return unauthorized when token is missing in request")
  void returnUnauthorizedWhenTokenIsMissingInRequest() {
    given()
        .contentType(APPLICATION_FORM_URLENCODED_VALUE)

        .when()
        .get("/oauth/check_token")

        .then()
        .log().ifValidationFails()
        .status(UNAUTHORIZED)
    ;
  }

  @Test
  @DisplayName("return unauthorized when using expired token")
  void returnUnauthorizedWhenUsingExpiredToken() {
    given()
        .param("token", "c1ffdd6c-a83b-496d-8335-febffac18fc4")
        .contentType(APPLICATION_FORM_URLENCODED_VALUE)

        .when()
        .get("/oauth/check_token")

        .then()
        .log().ifValidationFails()
        .status(UNAUTHORIZED)
    ;
  }

  private String getUserAccessToken() {
    return given()
        .param("grant_type", "external-token")
        .param("client_id", "makromapa-mobile")
        .header(GOOGLE_PREMIUM_USER.getAuthenticationHeader())
        .contentType(ContentType.JSON)

        .when()
        .post(EXTERNAL_AUTH_BASE_PATH + "/token")

        .then()
        .status(OK)
        .extract().body().jsonPath().getString("access_token");
  }

}