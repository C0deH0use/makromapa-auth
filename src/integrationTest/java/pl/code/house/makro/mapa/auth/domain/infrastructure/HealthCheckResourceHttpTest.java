package pl.code.house.makro.mapa.auth.domain.infrastructure;

import static io.restassured.http.ContentType.JSON;
import static io.restassured.module.mockmvc.RestAssuredMockMvc.given;
import static io.restassured.module.mockmvc.RestAssuredMockMvc.webAppContextSetup;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasKey;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;
import static org.springframework.http.HttpStatus.FORBIDDEN;
import static org.springframework.http.HttpStatus.NOT_FOUND;
import static org.springframework.http.HttpStatus.OK;
import static org.springframework.http.HttpStatus.UNAUTHORIZED;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static pl.code.house.makro.mapa.auth.domain.GreenMailSmtpConfig.SMTP_SETUP;

import com.icegreen.greenmail.util.GreenMail;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.web.context.WebApplicationContext;

@SpringBootTest(webEnvironment = RANDOM_PORT)
class HealthCheckResourceHttpTest {

  @Value("${info.app.name}")
  String appTitle;

  @Value("${info.app.description}")
  String appDesc;

  @Autowired
  WebApplicationContext context;

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
  @DisplayName("should display health status")
  void shouldDisplayHealthStatus() {
    //given
    given()
        .contentType(JSON)

        .when()
        .get("/actuator/health")

        .then()
        .log().all(true)
        .statusCode(OK.value())
        .body("status", equalTo("UP"))
        .body("components", hasKey("db"))
        .body("components", hasKey("diskSpace"))
        .body("components", hasKey("ping"))
        .body("components", hasKey("livenessState"))
        .body("components", hasKey("readinessState"))
        .body("groups", containsInAnyOrder("liveness", "readiness"))
    ;
  }

  @Test
  @DisplayName("should display readiness health status")
  void shouldDisplayReadinessHealthStatus() {
    //given
    given()
        .contentType(JSON)

        .when()
        .get("/actuator/health/readiness")

        .then()
        .log().ifValidationFails()
        .statusCode(OK.value())
        .body("status", equalTo("UP"))
    ;
  }

  @Test
  @DisplayName("should display liveness health status")
  void shouldDisplayLivenessHealthStatus() {
    //given
    given()
        .contentType(JSON)

        .when()
        .get("/actuator/health/liveness")

        .then()
        .log().ifValidationFails()
        .statusCode(OK.value())
        .body("status", equalTo("UP"))
    ;
  }

  @Test
  @DisplayName("should display info status")
  void shouldDisplayInfoStatus() {
    //given
    given()
        .contentType(JSON)
        .auth().with(httpBasic("admin_aga", "mysecretpassword"))

        .when()
        .get("/actuator/info")

        .then()
        .log().all(true)
        .statusCode(OK.value())

        .body("app.name", equalTo(appTitle))
        .body("app.description", equalTo(appDesc))
        .body("build.artifact", equalTo("makromapa-auth"))
        .body("build.name", equalTo("makromapa-auth"))
        .body("build.group", equalTo("pl.code.house.makro.mapa.auth"))
    ;
  }

  @Test
  @DisplayName("should return UNAUTHORIZED when asking for info status without credentials")
  void shouldReturnUnAuthorizedWhenAskingForInfoStatusWithoutCredentials() {
    //given
    given()
        .contentType(JSON)

        .when()
        .get("/actuator/info")

        .then()
        .statusCode(UNAUTHORIZED.value())
    ;
  }
}