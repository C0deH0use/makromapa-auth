package pl.code.house.makro.mapa.auth.domain.user;

import static io.restassured.module.mockmvc.RestAssuredMockMvc.given;
import static io.restassured.module.mockmvc.RestAssuredMockMvc.webAppContextSetup;
import static java.nio.charset.StandardCharsets.UTF_8;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.emptyOrNullString;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;
import static org.springframework.http.HttpHeaders.AUTHORIZATION;
import static org.springframework.http.HttpHeaders.encodeBasicAuth;
import static org.springframework.http.HttpStatus.OK;
import static org.springframework.http.HttpStatus.UNAUTHORIZED;
import static org.springframework.http.MediaType.APPLICATION_FORM_URLENCODED_VALUE;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static pl.code.house.makro.mapa.auth.ApiConstraints.BASE_PATH;
import static pl.code.house.makro.mapa.auth.domain.user.OAuth2Provider.BASIC_AUTH;
import static pl.code.house.makro.mapa.auth.domain.user.OAuth2Provider.GOOGLE;
import static pl.code.house.makro.mapa.auth.domain.user.TestUser.ADMIN;
import static pl.code.house.makro.mapa.auth.domain.user.TestUser.BEARER_TOKEN;
import static pl.code.house.makro.mapa.auth.domain.user.TestUser.GOOGLE_PREMIUM_USER;

import io.restassured.http.Header;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
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
        .body("provider", equalTo(GOOGLE.name()))
        .body("name", equalTo("Makromapa Test01"))
        .body("surname", equalTo("Test01"))
        .body("nickname", is(emptyOrNullString()))
        .body("email", equalTo("test.makro01@gmail.com"))
        .body("picture", notNullValue())
        .body("type", equalTo("PREMIUM_USER"))
        .body("enabled", equalTo(true))
    ;
  }

  @Test
  @Transactional
  @DisplayName("should update user info details")
  void shouldUpdateUserInfoDetails() {
    given()
        .contentType(APPLICATION_JSON_VALUE)
        .header(new Header(AUTHORIZATION, BEARER_TOKEN + GOOGLE_PREMIUM_USER.getAccessCode()))

        .param("name", "MakroMapa Premium")
        .param("surname", "MakroMapa")
        .param("nickname", "MakroMapa App")
        .param("picture", "picture1")

        .when()
        .post(BASE_PATH + "/user-info")

        .then()
        .log().ifValidationFails()
        .status(OK)

        .body("sub", equalTo(GOOGLE_PREMIUM_USER.getUserId().toString()))
        .body("provider", equalTo(GOOGLE.name()))
        .body("name", equalTo("MakroMapa Premium"))
        .body("surname", equalTo("MakroMapa"))
        .body("nickname", equalTo("MakroMapa App"))
        .body("email", equalTo("test.makro01@gmail.com"))
        .body("picture", equalTo("picture1"))
        .body("type", equalTo("PREMIUM_USER"))
        .body("enabled", equalTo(true))
    ;
  }

  @Test
  @DisplayName("should fetch user info for admin")
  void shouldFetchUserInfoForAdmin() {
    //given
    String adminAccessCode = given()
        .param("grant_type", "password")
        .param("username", ADMIN.getName())
        .param("password", ADMIN.getPassword())
        .header(new Header(AUTHORIZATION, "Basic " + encodeBasicAuth("basic-auth-makromapa-mobile", "secret", UTF_8)))
        .contentType(APPLICATION_FORM_URLENCODED_VALUE)

        .when()
        .post("/oauth/token")

        .then()
        .status(OK)
        .log().all(true)
        .body("scope", equalTo("USER"))
        .extract().body().jsonPath()
        .getString("access_token")
    ;

    given()
        .contentType(APPLICATION_JSON_VALUE)
        .header(new Header(AUTHORIZATION, BEARER_TOKEN + adminAccessCode))

        .when()
        .get(BASE_PATH + "/user-info")

        .then()
        .log().all(true)
        .status(OK)
        .body("sub", equalTo(ADMIN.getUserId().toString()))
        .body("provider", equalTo(BASIC_AUTH.name()))
        .body("name", is(emptyOrNullString()))
        .body("surname", is(emptyOrNullString()))
        .body("nickname", is(emptyOrNullString()))
        .body("email", equalTo(ADMIN.getName()))
        .body("picture", nullValue())
        .body("type", equalTo("ADMIN_USER"))
        .body("enabled", equalTo(true))
    ;
  }

  @Test
  @DisplayName("fetch default user avatars")
  void fetchDefaultUserAvatars() {
    given()
        .contentType(APPLICATION_JSON_VALUE)
        .header(new Header(AUTHORIZATION, BEARER_TOKEN + GOOGLE_PREMIUM_USER.getAccessCode()))

        .when()
        .get(BASE_PATH + "/user-info/avatars")

        .then()
        .log().ifValidationFails()
        .status(OK)

        .body("size()", greaterThanOrEqualTo(4))
        .body("$", hasItem(containsString("https://www.makromapa.pl")))
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

  @Test
  @DisplayName("return UNAUTHORIZED if not using any access code in request")
  void returnUnauthorizedIfNotUsingAnyAccessCodeInRequest() {
    given()
        .contentType(APPLICATION_JSON_VALUE)

        .when()
        .get(BASE_PATH + "/user-info")

        .then()
        .log().all(true)
        .status(UNAUTHORIZED)
    ;
  }


}