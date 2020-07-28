package pl.code.house.makro.mapa.auth.domain.user;

import static io.restassured.module.mockmvc.RestAssuredMockMvc.given;
import static io.restassured.module.mockmvc.RestAssuredMockMvc.webAppContextSetup;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;
import static org.springframework.http.HttpHeaders.AUTHORIZATION;
import static org.springframework.http.HttpStatus.OK;
import static org.springframework.http.HttpStatus.UNAUTHORIZED;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static pl.code.house.makro.mapa.auth.ApiConstraints.BASE_PATH;
import static pl.code.house.makro.mapa.auth.domain.user.OAuth2Provider.GOOGLE;
import static pl.code.house.makro.mapa.auth.domain.user.TestUser.BEARER_TOKEN;
import static pl.code.house.makro.mapa.auth.domain.user.TestUser.GOOGLE_PREMIUM_USER;

import io.restassured.http.Header;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.web.context.WebApplicationContext;

@SpringBootTest
class UserInfoResourceHttpTest {

  @Autowired
  private WebApplicationContext context;

  @BeforeEach
  void setup() {
    webAppContextSetup(context, springSecurity());
  }

  @Test
  @DisplayName("fetch user info with access token")
  void fetchUserInfoWithAccessToken() {
    given()
        .contentType(APPLICATION_JSON_VALUE)
        .header(new Header(AUTHORIZATION, BEARER_TOKEN + GOOGLE_PREMIUM_USER.getAccessCode()))
        .when()
        .get(BASE_PATH + "/user-info")

        .then()
        .log().all(true)
        .status(OK)
        .body("sub", equalTo(GOOGLE_PREMIUM_USER.getUserId().toString()))
        .body("externalId", equalTo(GOOGLE_PREMIUM_USER.getExternalId()))
        .body("provider", equalTo(GOOGLE.name()))
        .body("name", equalTo("Makromapa Test01"))
        .body("surname", equalTo("Test01"))
        .body("email", equalTo("test.makro01@gmail.com"))
        .body("picture", notNullValue())
        .body("type", equalTo("PREMIUM_USER"))
        .body("enabled", equalTo(true))
    ;
  }

  @Test
  @DisplayName("return UNAUTHORIZED if using jwt token for authentication to access user-info details")
  void returnUnauthorizedIfUsingJwtTokenForAuthenticationToAccessUserInfoDetails() {
    given()
        .contentType(APPLICATION_JSON_VALUE)
        .header(GOOGLE_PREMIUM_USER.getAuthenticationHeader())
        .when()
        .get(BASE_PATH + "/user-info")

        .then()
        .log().all(true)
        .status(UNAUTHORIZED)
        .body("error", equalTo("Token was not recognised"))
    ;
  }


}