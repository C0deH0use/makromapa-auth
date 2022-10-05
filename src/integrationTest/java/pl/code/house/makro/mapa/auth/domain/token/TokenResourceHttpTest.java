package pl.code.house.makro.mapa.auth.domain.token;

import static io.restassured.http.ContentType.JSON;
import static io.restassured.module.mockmvc.RestAssuredMockMvc.given;
import static io.restassured.module.mockmvc.RestAssuredMockMvc.webAppContextSetup;
import static java.nio.charset.StandardCharsets.UTF_8;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasItems;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.notNullValue;
import static org.springframework.http.HttpHeaders.AUTHORIZATION;
import static org.springframework.http.HttpHeaders.encodeBasicAuth;
import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.FORBIDDEN;
import static org.springframework.http.HttpStatus.OK;
import static org.springframework.http.HttpStatus.UNAUTHORIZED;
import static org.springframework.http.MediaType.APPLICATION_FORM_URLENCODED_VALUE;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static pl.code.house.makro.mapa.auth.ApiConstraints.EXTERNAL_AUTHENTICATION_PATH;
import static pl.code.house.makro.mapa.auth.domain.user.TestUser.ADMIN;
import static pl.code.house.makro.mapa.auth.domain.user.TestUser.GOOGLE_PREMIUM_USER;
import static pl.code.house.makro.mapa.auth.domain.user.TestUser.REG_USER;

import io.restassured.http.Header;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;
import pl.code.house.makro.mapa.auth.domain.user.TestUserRepository;

@SpringBootTest
class TokenResourceHttpTest {

  private static final String ACCESS_TOKEN = "10c7fc72-64f6-4c9a-af2b-a5d33c65ecf3";

  @Autowired
  private WebApplicationContext context;

  @Autowired
  private TestUserRepository userRepository;

  @BeforeEach
  void setup() {
    webAppContextSetup(context, springSecurity());
  }

  @Test
  @Transactional
  @DisplayName("return OK with new token")
  void returnOkWithNewToken() {
    given()
        .param("grant_type", "password")
        .param("username", REG_USER.getName())
        .param("password", REG_USER.getPassword())
        .header(new Header(AUTHORIZATION, "Basic " + encodeBasicAuth("basic-auth-makromapa-mobile", "secret", UTF_8)))
        .contentType(APPLICATION_FORM_URLENCODED_VALUE)
        .log().all(true)

        .when()
        .post("/oauth/token")

        .then()
        .log().all(true)
        .status(OK)

        .body("token_type", equalTo("bearer"))
        .body("scope", equalTo("USER"))
        .body("access_token", notNullValue())
        .body("expires_in", greaterThanOrEqualTo(0))
    ;
  }

  @Test
  @Disabled
  @Transactional
  @DisplayName("return token details when requesting with valid access token")
  void returnTokenDetailsWhenRequestingWithValidAccessToken() {
    String userAccessToken = getUserAccessToken();

    given()
        .param("token", userAccessToken)
        .header(new Header(AUTHORIZATION, "Basic " + encodeBasicAuth("makromapa-backend", "secret", UTF_8)))
        .contentType(APPLICATION_FORM_URLENCODED_VALUE)

        .when()
        .post("/oauth/check_token")

        .then()
        .log().all()
        .status(OK)
        .body("active", equalTo(true))
        .body("exp", notNullValue())
        .body("aud", hasItem("makromapa-mobile"))
        .body("sub", equalTo("aa6641c1-e9f4-417f-adf4-f71accc470cb"))
        .body("client_id", equalTo("makromapa-mobile"))
        .body("scope", hasItems("PREMIUM", "FREE_USER", "DISABLE_ADS"))
        .body("authorities", hasItems("ROLE_FREE_USER", "ROLE_DISABLE_ADS", "ROLE_PREMIUM"))
    ;
  }

  @Test
  @Transactional
  @DisplayName("should check token owned by admin when logged in from mobile app")
  void shouldCheckTokenOwnedByAdmin() {
    String adminAccessToken = getAdminAccessTokenLoggedInFromMobileApp();
    String adminId = userRepository.findIdByEmail(ADMIN.getName());
    given()
        .param("token", adminAccessToken)
        .header(new Header(AUTHORIZATION, "Basic " + encodeBasicAuth("makromapa-backend", "secret", UTF_8)))
        .contentType(APPLICATION_FORM_URLENCODED_VALUE)

        .when()
        .post("/oauth/check_token")

        .then()
        .log().all()
        .status(OK)
        .body("active", equalTo(true))
        .body("exp", notNullValue())
        .body("sub", equalTo(adminId))
        .body("client_id", equalTo("basic-auth-makromapa-mobile"))
        .body("scope", hasSize(1))
        .body("scope", hasItems("USER"))
        .body("authorities", hasItems("ROLE_ADMIN_USER"))
    ;
  }

  @Test
  @Transactional
  @DisplayName("should check token owned by admin when logged in from Admin app")
  void shouldCheckTokenOwnedByAdminLoggedInFromAdminApp() {
    String adminAccessToken = getAdminAccessTokenLoggedInFromAdminClient();
    given()
        .param("token", adminAccessToken)
        .header(new Header(AUTHORIZATION, "Basic " + encodeBasicAuth("makromapa-backend", "secret", UTF_8)))
        .contentType(APPLICATION_FORM_URLENCODED_VALUE)

        .when()
        .post("/oauth/check_token")

        .then()
        .log().all()
        .status(OK)
        .body("active", equalTo(true))
        .body("exp", notNullValue())
        .body("client_id", equalTo("makromapa-admin"))
        .body("scope", hasSize(2))
        .body("scope", hasItems("USER"))
        .body("scope", hasItems("ADMIN"))
        .body("authorities", hasItems("ROLE_ADMIN_USER"))
    ;
  }

  @Test
  @Transactional
  @DisplayName("should check token owned by backend system")
  void shouldCheckTokenOwnedByBackendSystem() {
    String backendSystemAccessToken = getBackendSystemAccessToken();
    given()
        .param("token", backendSystemAccessToken)
        .header(new Header(AUTHORIZATION, "Basic " + encodeBasicAuth("makromapa-backend", "secret", UTF_8)))
        .contentType(APPLICATION_FORM_URLENCODED_VALUE)

        .when()
        .post("/oauth/check_token")

        .then()
        .log().all()
        .status(OK)
        .body("active", equalTo(true))
        .body("exp", notNullValue())
        .body("client_id", equalTo("makromapa-backend"))
        .body("scope", hasSize(1))
        .body("scope", hasItems("USER"))
        .body("authorities", hasItem("ROLE_MAKROMAPA_BACKEND"))
    ;
  }

  @Test
  @DisplayName("return FORBIDDEN when requesting token details by an unauthorized user")
  void returnForbiddenWhenRequestingTokenDetailsByAnUnauthorizedUser() {
    given()
        .param("token", ACCESS_TOKEN)
        .header(new Header(AUTHORIZATION, "Basic " + encodeBasicAuth("makromapa-admin", "secret", UTF_8)))
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
        .header(new Header(AUTHORIZATION, "Basic " + encodeBasicAuth("makromapa-backend", "secret", UTF_8)))

        .when()
        .post("/oauth/check_token")

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
        .post("/oauth/check_token")

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
        .post("/oauth/check_token")

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
        .post("/oauth/check_token")

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
        .contentType(JSON)

        .when()
        .post(EXTERNAL_AUTHENTICATION_PATH + "/token")

        .then()
        .status(OK)
        .extract().body().jsonPath().getString("access_token");
  }

  private String getAdminAccessTokenLoggedInFromMobileApp() {
    return given()
        .param("grant_type", "password")
        .param("client_id", "basic-auth-makromapa-mobile")
        .param("username", ADMIN.getName())
        .param("password", ADMIN.getPassword())
        .header(new Header(AUTHORIZATION, "Basic " + encodeBasicAuth("basic-auth-makromapa-mobile", "secret", UTF_8)))
        .contentType(JSON)

        .when()
        .post("/oauth/token")

        .then()
        .status(OK)
        .extract().body().jsonPath().getString("access_token");
  }

  private String getAdminAccessTokenLoggedInFromAdminClient() {
    return given()
        .param("grant_type", "client_credentials")
        .param("client_id", "makromapa-admin")
        .param("username", ADMIN.getName())
        .param("password", ADMIN.getPassword())
        .header(new Header(AUTHORIZATION, "Basic " + encodeBasicAuth("makromapa-admin", "secret", UTF_8)))
        .contentType(JSON)

        .when()
        .post("/oauth/token")

        .then()
        .status(OK)
        .extract().body().jsonPath().getString("access_token");
  }

  private String getBackendSystemAccessToken() {
    return given()
        .param("grant_type", "client_credentials")
        .param("clientId", "makromapa-backend")
        .param("clientSecret", "secret")
        .header(new Header(AUTHORIZATION, "Basic " + encodeBasicAuth("makromapa-backend", "secret", UTF_8)))
        .contentType(JSON)

        .when()
        .post("/oauth/token")

        .then()
        .status(OK)
        .extract().body().jsonPath().getString("access_token");
  }

}