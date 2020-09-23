package pl.code.house.makro.mapa.auth.domain.management;

import static io.restassured.module.mockmvc.RestAssuredMockMvc.given;
import static io.restassured.module.mockmvc.RestAssuredMockMvc.webAppContextSetup;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;
import static org.mockito.ArgumentMatchers.notNull;
import static org.springframework.http.HttpHeaders.AUTHORIZATION;
import static org.springframework.http.HttpStatus.OK;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.jdbc.JdbcTestUtils.countRowsInTable;
import static pl.code.house.makro.mapa.auth.ApiConstraints.BASE_PATH;
import static pl.code.house.makro.mapa.auth.domain.user.TestUser.BEARER_TOKEN;
import static pl.code.house.makro.mapa.auth.domain.user.TestUser.GOOGLE_PREMIUM_USER;
import static pl.code.house.makro.mapa.auth.domain.user.TestUser.REG_USER;

import io.restassured.http.Header;
import java.util.Map;
import org.assertj.core.api.IntegerAssert;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;

@SpringBootTest
class UserOptOutResourceHttpTest {

  @Autowired
  private WebApplicationContext context;

  @Autowired
  private JdbcTemplate jdbcTemplate;

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

  @Test
  @Transactional
  @DisplayName("try to optout from the program")
  void tryToOptoutFromTheProgram() {
    given()
        .contentType(APPLICATION_JSON_VALUE)
        .header(new Header(AUTHORIZATION, BEARER_TOKEN + GOOGLE_PREMIUM_USER.getAccessCode()))
        .when()
        .delete(BASE_PATH + "/user/optout")

        .then()
        .log().all(true)
        .status(OK)
    ;

    assertAccessTokenCount().isEqualTo(0);
  }

  private IntegerAssert assertAccessTokenCount() {
    return new IntegerAssert(countRowsInTable(jdbcTemplate, "oauth_access_token"));
  }
}