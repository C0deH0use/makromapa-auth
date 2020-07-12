package pl.code.house.makro.mapa.auth.domain.user;

import static io.restassured.module.mockmvc.RestAssuredMockMvc.given;
import static java.time.ZonedDateTime.now;
import static org.assertj.core.api.Assertions.assertThat;
import static org.exparity.hamcrest.date.ZonedDateTimeMatchers.sameMinuteOfHour;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.notNullValue;
import static org.springframework.http.HttpStatus.OK;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static pl.code.house.makro.mapa.auth.ApiConstraints.AUTH_BASE_PATH;
import static pl.code.house.makro.mapa.auth.domain.user.TestUser.GOOGLE_NEW_USER;
import static pl.code.house.makro.mapa.auth.domain.user.TestUser.GOOGLE_PREMIUM_USER;

import io.restassured.http.ContentType;
import io.restassured.module.mockmvc.RestAssuredMockMvc;
import java.time.Clock;
import java.time.ZonedDateTime;
import org.hamcrest.MatcherAssert;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;
import pl.code.house.makro.mapa.auth.domain.token.TestAccessTokenRepository;

@SpringBootTest
@EnableTransactionManagement
class UserAuthorizationResourceHttpTest {

  @Autowired
  private WebApplicationContext context;

  @Autowired
  private TestUserRepository userRepository;

  @Autowired
  private TestAccessTokenRepository accessTokenRepository;

  @Autowired
  private Clock clock;

  @BeforeEach
  void setup() {
    RestAssuredMockMvc.webAppContextSetup(context, springSecurity());
  }

  @Test
  @Transactional
  @DisplayName("should map jwt token to existing user and create new access token")
  void shouldMapJwtTokenToExistingUserAndCreateNewAccessToken() {
    //given
    assertThat(userRepository.count()).isEqualTo(1);
    assertThat(accessTokenRepository.findByUserId(1000)).hasSize(1);

    String expiryDateStr = given()
        .header(GOOGLE_PREMIUM_USER.getAuthenticationHeader())
        .contentType(ContentType.JSON)

        .when()
        .post(AUTH_BASE_PATH + "/authorize")

        .then()
        .log().ifValidationFails()
        .status(OK)
        .body("userId", equalTo(1000))
        .body("code", notNullValue())
        .body("refreshCode", notNullValue())
        .body("refreshCodeExpiryDate", notNullValue())
        .extract().body().path("expiryDate");

    ZonedDateTime expiryDate = ZonedDateTime.parse(expiryDateStr);
    MatcherAssert.assertThat(expiryDate, sameMinuteOfHour(now(clock).plusHours(6)));

    assertThat(userRepository.count()).isEqualTo(1);
    assertThat(accessTokenRepository.findByUserId(1000)).hasSize(2);
    assertThat(accessTokenRepository.findActiveByUserId(1000)).hasSize(1);
  }

  @Test
  @Transactional
  @DisplayName("should convert jwt token for new user and create a new token")
  void shouldConvertJwtTokenForNewUserAndCreateANewToken() {
    //given
    assertThat(userRepository.count()).isEqualTo(1);
    assertThat(accessTokenRepository.findByUserId(1001)).hasSize(0);

    String expiryDateStr = given()
        .header(GOOGLE_NEW_USER.getAuthenticationHeader())
        .contentType(ContentType.JSON)

        .when()
        .post(AUTH_BASE_PATH + "/authorize")

        .then()
        .log().ifValidationFails()
        .status(OK)
        .body("userId", greaterThanOrEqualTo(1001))
        .body("code", notNullValue())
        .body("refreshCode", notNullValue())
        .body("refreshCodeExpiryDate", notNullValue())
        .extract().body().path("expiryDate");

    ZonedDateTime expiryDate = ZonedDateTime.parse(expiryDateStr);
    MatcherAssert.assertThat(expiryDate, sameMinuteOfHour(now(clock).plusHours(6)));

    assertThat(userRepository.count()).isEqualTo(2);
    assertThat(accessTokenRepository.findByUserId(1001)).hasSize(1);
  }
}