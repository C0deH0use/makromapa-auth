package pl.code.house.makro.mapa.auth.domain.management;

import static io.restassured.module.mockmvc.RestAssuredMockMvc.given;
import static io.restassured.module.mockmvc.RestAssuredMockMvc.webAppContextSetup;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;
import static org.mockito.ArgumentMatchers.notNull;
import static org.springframework.http.HttpStatus.OK;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static pl.code.house.makro.mapa.auth.domain.user.TestUser.REG_USER;

import java.util.Map;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.web.context.WebApplicationContext;

@SpringBootTest
class UserOptOutResourceHttpTest {

  @Autowired
  private WebApplicationContext context;

  @BeforeEach
  void setup() {
    webAppContextSetup(context, springSecurity());
  }

  @Test
  @DisplayName("should accept unauthorized request with signed body")
  void shouldAcceptUnauthorizedRequestWithSignedBody() {
    given()
        .param("signedRequest", REG_USER.getPassword())
        .contentType(APPLICATION_JSON_VALUE)

        .when()
        .post("/oauth/external/user/facebook/opt-out")

        .then()
        .log().all(true)
        .status(OK)
        .body("url", notNullValue())
        .body("confirmation_code", equalTo("200"))
    ;
  }
}