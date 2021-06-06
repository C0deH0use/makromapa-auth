package pl.code.house.makro.mapa.auth.domain.infrastructure;

import static io.restassured.module.mockmvc.RestAssuredMockMvc.given;
import static io.restassured.module.mockmvc.RestAssuredMockMvc.webAppContextSetup;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;
import static org.springframework.cloud.contract.spec.internal.HttpStatus.NOT_FOUND;
import static org.springframework.cloud.contract.spec.internal.HttpStatus.OK;
import static org.springframework.cloud.contract.spec.internal.HttpStatus.UNAUTHORIZED;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.web.context.WebApplicationContext;

@SpringBootTest(webEnvironment = RANDOM_PORT)
class ActuatorConfigurationHttpTest {

  @Autowired
  WebApplicationContext context;

  @BeforeEach
  void setup() {
    webAppContextSetup(context, springSecurity());
  }

  @Test
  @DisplayName("Logfile Actuator endpoint should is UNAUTHORIZED")
  void LogfileEndpointIsUnauthorized() {
    given()
        .get("/actuator/logfile")

        .then()
        .log().ifValidationFails()
        .statusCode(UNAUTHORIZED);
  }

  @Test
  @DisplayName("Loggers Actuator endpoint should is UNAUTHORIZED")
  void LoggersEndpointIsUnauthorized() {
    given()
        .get("/actuator/loggers")

        .then()
        .log().ifValidationFails()
        .statusCode(UNAUTHORIZED);
  }

  @Test
  @DisplayName("Logfile Actuator endpoint should is Not Opened despite being Admin")
  void logfileActuatorEndpointShouldIsNotOpenedDespiteBeingAdmin() {
    given()
        .auth().with(httpBasic("admin_aga", "mysecretpassword"))
        .get("/actuator/logfile")

        .then()
        .log().ifValidationFails()
        .statusCode(NOT_FOUND);
  }

  @Test
  @DisplayName("Logger actuator endpoint is allowed only for admins")
  void loggerActuatorEndpointIsAllowedOnlyForAdmins() {
    given()
        .auth().with(httpBasic("admin_aga", "mysecretpassword"))

        .get("/actuator/loggers")

        .then()
        .log().ifValidationFails()
        .statusCode(OK);
  }
}
