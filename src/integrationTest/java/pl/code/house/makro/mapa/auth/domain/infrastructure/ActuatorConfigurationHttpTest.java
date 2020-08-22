package pl.code.house.makro.mapa.auth.domain.infrastructure;

import static io.restassured.RestAssured.given;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;

import io.restassured.RestAssured;
import io.restassured.module.mockmvc.RestAssuredMockMvc;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.actuate.autoconfigure.web.server.LocalManagementPort;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.web.context.WebApplicationContext;

@SpringBootTest(webEnvironment = RANDOM_PORT)
class ActuatorConfigurationHttpTest {
  @Autowired
  WebApplicationContext context;

  @LocalManagementPort
  int port;

  @BeforeEach
  void setup() {
    RestAssured.port = port;
    RestAssuredMockMvc.webAppContextSetup(context, springSecurity());
  }

  @Test
  @DisplayName("Logfile Actuator endpoint should is not open")
  void LogfileEndpointNotOpen() {
    given()
        .get("/actuator/logfile")

        .then()
        .log().ifValidationFails()
        .statusCode(404);
  }

  @Test
  @DisplayName("Loggers Actuator endpoint should is not open")
  void LoggersEndpointOpen() {
    given()
        .get("/actuator/loggers")

        .then()
        .log().ifValidationFails()
        .statusCode(200);
  }
}
