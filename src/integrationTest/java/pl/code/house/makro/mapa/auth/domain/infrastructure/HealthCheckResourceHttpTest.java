package pl.code.house.makro.mapa.auth.domain.infrastructure;


import static io.restassured.http.ContentType.JSON;
import static io.restassured.module.mockmvc.RestAssuredMockMvc.given;
import static io.restassured.module.mockmvc.RestAssuredMockMvc.webAppContextSetup;
import static org.hamcrest.Matchers.equalTo;
import static org.springframework.http.HttpStatus.OK;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static pl.code.house.makro.mapa.auth.domain.GreenMailSmtpConfig.SMTP_SETUP;

import com.icegreen.greenmail.util.GreenMail;
import org.junit.Ignore;
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
  @Ignore
  @DisplayName("return health status")
  void returnHealthStatus() {
    //given
    given()
        .contentType(JSON)

        .when()
        .get("/health")

        .then()
        .log().ifValidationFails()
        .status(OK)
    ;
  }

  @Test
  @DisplayName("return info status")
  void returnInfoStatus() {
    //given
    given()
        .contentType(JSON)

        .when()
        .get("/info")

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
}