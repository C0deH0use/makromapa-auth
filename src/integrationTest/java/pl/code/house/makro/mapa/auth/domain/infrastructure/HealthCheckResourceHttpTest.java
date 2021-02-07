package pl.code.house.makro.mapa.auth.domain.infrastructure;


import static com.icegreen.greenmail.configuration.GreenMailConfiguration.aConfig;
import static io.restassured.http.ContentType.JSON;
import static io.restassured.module.mockmvc.RestAssuredMockMvc.given;
import static org.hamcrest.Matchers.equalTo;
import static org.springframework.http.HttpStatus.OK;
import static pl.code.house.makro.mapa.auth.domain.GreenMailSmtpConfig.SMTP_SETUP;

import com.icegreen.greenmail.junit5.GreenMailExtension;
import org.junit.Ignore;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
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

  @RegisterExtension
  static GreenMailExtension mailBean = new GreenMailExtension(SMTP_SETUP)
      .withConfiguration(aConfig()
          .withUser("user_greenMain", "secret_password")
      );

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