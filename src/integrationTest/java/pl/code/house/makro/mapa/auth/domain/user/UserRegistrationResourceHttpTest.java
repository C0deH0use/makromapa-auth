package pl.code.house.makro.mapa.auth.domain.user;

import static io.restassured.module.mockmvc.RestAssuredMockMvc.given;
import static io.restassured.module.mockmvc.RestAssuredMockMvc.webAppContextSetup;
import static java.nio.charset.StandardCharsets.UTF_8;
import static org.springframework.http.HttpHeaders.AUTHORIZATION;
import static org.springframework.http.HttpHeaders.encodeBasicAuth;
import static org.springframework.http.HttpStatus.CREATED;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static pl.code.house.makro.mapa.auth.ApiConstraints.BASE_PATH;

import io.restassured.http.Header;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;

@SpringBootTest
class UserRegistrationResourceHttpTest {

  @Autowired
  private WebApplicationContext context;

  @BeforeEach
  void setup() {
    webAppContextSetup(context, springSecurity());
  }

  @Test
  @Transactional
  @DisplayName("register new user by mobile client")
  void registerNewUserByMobileClient() {
    given()
        .param("grant_type", "external-token")
        .param("client_id", "makromapa-mobile")
        .param("username", "marek.malik@itds.pl")
        .param("password", "secret")
        .contentType(APPLICATION_JSON_VALUE)
        .header(new Header(AUTHORIZATION, "Basic " + encodeBasicAuth("makromapa-mobile", "secret", UTF_8)))

        .when()
        .post(BASE_PATH + "/user")

        .then()
        .log().all()
        .status(CREATED)
    ;
  }
}