package pl.code.house.makro.mapa.auth.domain.token;

import static io.restassured.module.mockmvc.RestAssuredMockMvc.given;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;
import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.OK;
import static org.springframework.http.HttpStatus.UNAUTHORIZED;
import static org.springframework.http.MediaType.APPLICATION_FORM_URLENCODED_VALUE;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static pl.code.house.makro.mapa.auth.ApiConstraints.AUTH_BASE_PATH;
import static pl.code.house.makro.mapa.auth.domain.user.TestUser.GOOGLE_PREMIUM_USER;

import io.restassured.module.mockmvc.RestAssuredMockMvc;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.web.context.WebApplicationContext;

@SpringBootTest
class TokenResourceHttpTest {

  @Autowired
  private WebApplicationContext context;

  @BeforeEach
  void setup() {
    RestAssuredMockMvc.webAppContextSetup(context, springSecurity());
  }

  @Test
  @DisplayName("return OK when using correct valid token")
  void returnOkWhenUsingCorrectValidToken() {
    given()
        .param("token", GOOGLE_PREMIUM_USER.getAccessToken())

        .contentType(APPLICATION_FORM_URLENCODED_VALUE)

        .when()
        .post(AUTH_BASE_PATH + "/token/introspect")

        .then()
        .log().ifValidationFails()
        .status(OK)

        .body("username", equalTo(1000))
        .body("active", equalTo(true))
        .body("scope", equalTo("MAKROMAPA"))
        .body("client_id", equalTo("123456789"))
        .body("exp", notNullValue())
    ;
  }

  @Test
  @DisplayName("return unauthorized when token is with invalid token type")
  void returnUnauthorizedWhenTokenIsWithInvalidTokenType() {
    given()
        .param("token", "")
        .contentType(APPLICATION_FORM_URLENCODED_VALUE)

        .when()
        .post(AUTH_BASE_PATH + "/token/introspect")

        .then()
        .log().ifValidationFails()
        .status(UNAUTHORIZED)
    ;
  }

  @Test
  @DisplayName("return BadRequest when token is missing in request")
  void returnBadRequestWhenTokenIsMissingInRequest() {
    given()
        .contentType(APPLICATION_FORM_URLENCODED_VALUE)

        .when()
        .post(AUTH_BASE_PATH + "/token/introspect")

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
        .post(AUTH_BASE_PATH + "/token/introspect")

        .then()
        .log().ifValidationFails()
        .status(UNAUTHORIZED)
    ;
  }

}