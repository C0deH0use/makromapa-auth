package pl.code.house.makro.mapa.auth.domain.infrastructure;

import static io.restassured.http.ContentType.JSON;
import static io.restassured.module.mockmvc.RestAssuredMockMvc.given;
import static io.restassured.module.mockmvc.RestAssuredMockMvc.webAppContextSetup;
import static org.hamcrest.Matchers.equalTo;
import static org.springframework.http.HttpHeaders.AUTHORIZATION;
import static org.springframework.http.HttpStatus.FORBIDDEN;
import static org.springframework.http.HttpStatus.OK;
import static org.springframework.http.HttpStatus.UNAUTHORIZED;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static pl.code.house.makro.mapa.auth.domain.GreenMailSmtpConfig.SMTP_SETUP;
import static pl.code.house.makro.mapa.auth.domain.user.TestUser.BEARER_TOKEN;
import static pl.code.house.makro.mapa.auth.domain.user.TestUser.GOOGLE_PREMIUM_USER;

import com.icegreen.greenmail.util.GreenMail;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.web.context.WebApplicationContext;

@SpringBootTest
class HealthCheckResourceHttpTest {

  @Value("${info.app.name}")
  String appTitle;

  @Value("${info.app.description}")
  String appDesc;

  @Autowired
  private WebApplicationContext context;

  private GreenMail greenMail;

  @BeforeEach
  void setup() {
    greenMail = new GreenMail(SMTP_SETUP);
    greenMail.setUser("user_greenMain", "secret_password");
    greenMail.start();

    webAppContextSetup(context, springSecurity());
  }

  @AfterEach
  void stop() {
    greenMail.stop();
  }

  @Test
  @DisplayName("should display health status when user is admin")
  void shouldDisplayHealthStatusWhenUserIsAdmin() {
    //given
    given()
        .auth().with(httpBasic("admin_aga", "mysecretpassword"))

        .contentType(JSON)

        .when()
        .get("/actuator/health")

        .then()
        .log().ifValidationFails()
        .status(OK)
    ;
  }

  @Test
  @DisplayName("should not allow health status when user is not authenticated")
  void shouldNotAllowHealthStatusWhenUserIsNotAuthenticated() {
    //given
    given()
        .contentType(JSON)
        .when()
        .get("/actuator/health")

        .then()
        .status(UNAUTHORIZED)
    ;
  }

  @Test
  @DisplayName("return info status when asking as BasicAuth Admin")
  void returnInfoStatusWhenAskingAsBasicAuthAdmin() {
    //given
    given()
        .auth().with(httpBasic("admin_aga", "mysecretpassword"))

        .contentType(JSON)

        .when()
        .get("/actuator/info")

        .then()
        .log().all(true)
        .status(OK)
        .body("app.name", equalTo(appTitle))
        .body("app.description", equalTo(appDesc))
        .body("build.artifact", equalTo("makromapa-auth"))
        .body("build.name", equalTo("makromapa-auth"))
        .body("build.group", equalTo("pl.code.house.makro.mapa.auth"))
    ;
  }

  @Test
  @DisplayName("should not allow info status when user is not authenticated")
  void shouldNotAllowInfoStatusWhenUserIsNotAuthenticated() {
    //given
    given()
        .contentType(JSON)
        .when()
        .get("/actuator/info")

        .then()
        .status(UNAUTHORIZED)
    ;
  }

  @Test
  @DisplayName("should not allow info status for OAuth2 user2")
  void shouldNotAllowInfoStatusForOAuth2User2() {
    //given
    given()
        .header(AUTHORIZATION, BEARER_TOKEN + GOOGLE_PREMIUM_USER.getAccessCode())
        .contentType(JSON)

        .when()
        .get("/actuator/info")

        .then()
        .status(FORBIDDEN)
    ;
  }
}