package pl.code.house.makro.mapa.auth.domain.token;

import static io.restassured.module.mockmvc.RestAssuredMockMvc.given;
import static java.nio.charset.StandardCharsets.UTF_8;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.lessThanOrEqualTo;
import static org.hamcrest.Matchers.notNullValue;
import static org.springframework.http.HttpHeaders.encodeBasicAuth;
import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.OK;
import static org.springframework.http.MediaType.APPLICATION_FORM_URLENCODED_VALUE;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static pl.code.house.makro.mapa.auth.domain.user.TestUser.GOOGLE_PREMIUM_USER;

import io.restassured.http.Header;
import io.restassured.module.mockmvc.RestAssuredMockMvc;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;
import pl.code.house.makro.mapa.auth.domain.user.TestUser;

@SpringBootTest
class TokenResourceHttpTest {

  private final PasswordEncoder encoder = new BCryptPasswordEncoder();
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
            .header(new Header(HttpHeaders.AUTHORIZATION, "Basic " + encodeBasicAuth("makromapa-mobile", "mobile-apps-secret", UTF_8)))
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
  @DisplayName("return token details when requesting with valid access token")
  void returnTokenDetailsWhenRequestingWithValidAccessToken() {
    given()
        .param("token", GOOGLE_PREMIUM_USER.getAccessToken())
        .contentType(APPLICATION_FORM_URLENCODED_VALUE)

        .when()
        .get("/oauth/check_token")

        .then()
        .log().all()
        .status(OK)
    ;
  }

  @Test
  @DisplayName("return unauthorized when token is with invalid token type")
  void returnUnauthorizedWhenTokenIsWithInvalidTokenType() {
    given()
        .param("token", "")
        .contentType(APPLICATION_FORM_URLENCODED_VALUE)

        .when()
        .get("/oauth/check_token")

        .then()
        .log().ifValidationFails()
        .status(BAD_REQUEST)
        .body("error", equalTo("invalid_token"))
    ;
  }

  @Test
  @DisplayName("return BadRequest when token is missing in request")
  void returnBadRequestWhenTokenIsMissingInRequest() {
    given()
        .contentType(APPLICATION_FORM_URLENCODED_VALUE)

        .when()
        .get("/oauth/check_token")

        .then()
        .log().ifValidationFails()
        .status(BAD_REQUEST)
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
        .status(BAD_REQUEST)
    ;
  }

}