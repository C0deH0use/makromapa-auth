package pl.code.house.makro.mapa.auth.domain.user;

import static io.restassured.module.mockmvc.RestAssuredMockMvc.given;
import static io.restassured.module.mockmvc.RestAssuredMockMvc.webAppContextSetup;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.http.HttpHeaders.AUTHORIZATION;
import static org.springframework.http.HttpStatus.OK;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static pl.code.house.makro.mapa.auth.ApiConstraints.BASE_PATH;
import static pl.code.house.makro.mapa.auth.domain.user.TestUser.BEARER_TOKEN;
import static pl.code.house.makro.mapa.auth.domain.user.TestUser.GOOGLE_PREMIUM_USER;

import io.restassured.http.Header;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.Rollback;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;
import pl.code.house.makro.mapa.auth.domain.token.TestOAuthAccessTokenRepository;

@SpringBootTest
class UserOptOutResourceHttpTest {

  @Autowired
  private WebApplicationContext context;

  @Autowired
  private TestOAuthAccessTokenRepository oAuthAccessTokenRepository;

  @Autowired
  private TestUserRepository userRepository;

  @BeforeEach
  void setup() {
    webAppContextSetup(context, springSecurity());
  }

  @Test
  @Rollback
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

    assertThat(oAuthAccessTokenRepository.count()).isZero();
    assertThat(userRepository.countByExternalId(GOOGLE_PREMIUM_USER.getExternalId())).isZero();
  }
}