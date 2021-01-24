package pl.code.house.makro.mapa.auth.domain.user;

import static io.restassured.module.mockmvc.RestAssuredMockMvc.given;
import static io.restassured.module.mockmvc.RestAssuredMockMvc.webAppContextSetup;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.emptyOrNullString;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasItems;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.springframework.http.HttpHeaders.AUTHORIZATION;
import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.CONFLICT;
import static org.springframework.http.HttpStatus.OK;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static pl.code.house.makro.mapa.auth.ApiConstraints.BASE_PATH;
import static pl.code.house.makro.mapa.auth.domain.user.OAuth2Provider.GOOGLE;
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

@SpringBootTest
class UserPointsResourceHttpTest {

  @Autowired
  private WebApplicationContext context;

  @Autowired
  private UserRepository repository;

  @BeforeEach
  void setup() {
    webAppContextSetup(context, springSecurity());
  }

  @Test
  @Rollback
  @Transactional
  @DisplayName("should update user points by earning 100")
  void shouldUpdateUserPointsByEarning100() {
    //given
    int pointsEarned = 100;
    assertThat(repository.findById(GOOGLE_PREMIUM_USER.getUserId())).map(BaseUser::getUserDetails).map(UserDetails::getPoints).hasValue(0);

    given()
        .contentType(APPLICATION_JSON_VALUE)
        .header(new Header(AUTHORIZATION, BEARER_TOKEN + GOOGLE_PREMIUM_USER.getAccessCode()))
        .param("operation", "EARN")
        .param("product", "1000")

        .when()
        .post(BASE_PATH + "/user/points")

        .then()
        .log().all(true)
        .status(OK)
        .body("sub", equalTo(GOOGLE_PREMIUM_USER.getUserId().toString()))
        .body("provider", equalTo(GOOGLE.name()))
        .body("name", is(GOOGLE_PREMIUM_USER.getName()))
        .body("surname", is(not(emptyOrNullString())))
        .body("nickname", is(emptyOrNullString()))
        .body("email", is(not(emptyOrNullString())))
        .body("picture", is(not(emptyOrNullString())))
        .body("type", equalTo("FREE_USER"))
        .body("premiumFeatures", hasItems("PREMIUM", "DISABLE_ADS"))
        .body("points", equalTo(pointsEarned))
        .body("enabled", equalTo(true));

    //then
    assertThat(repository.findById(GOOGLE_PREMIUM_USER.getUserId())).map(BaseUser::getUserDetails).map(UserDetails::getPoints).hasValue(pointsEarned);
  }

  @Test
  @DisplayName("should return BAD_REQUEST when using invalid operation with given product")
  void shouldReturnBadRequestWhenUsingInvalidOperationWithGivenProduct() {
    given()
        .contentType(APPLICATION_JSON_VALUE)
        .header(new Header(AUTHORIZATION, BEARER_TOKEN + GOOGLE_PREMIUM_USER.getAccessCode()))
        .param("operation", "EARN")
        .param("product", "1003")

        .when()
        .post(BASE_PATH + "/user/points")

        .then()
        .log().all(true)
        .status(CONFLICT)
        .body("error", equalTo("Product `PURCHASE_1000` does not accept following operation reason to assign points to user:EARN"));

    //then
    assertThat(repository.findById(GOOGLE_PREMIUM_USER.getUserId())).map(BaseUser::getUserDetails).map(UserDetails::getPoints).hasValue(0);
  }

  @Test
  @DisplayName("should return BAD_REQUEST when using unknown points product")
  void shouldReturnBadRequestWhenUsingUnknownPointsProduct() {
    //given
    given()
        .contentType(APPLICATION_JSON_VALUE)
        .header(new Header(AUTHORIZATION, BEARER_TOKEN + GOOGLE_PREMIUM_USER.getAccessCode()))
        .param("operation", "EARN")
        .param("product", "1200")

        .when()
        .post(BASE_PATH + "/user/points")

        .then()
        .log().all(true)
        .status(BAD_REQUEST)
        .body("error", equalTo("Could not find product to because of which points where earned"));

    //then
    assertThat(repository.findById(GOOGLE_PREMIUM_USER.getUserId())).map(BaseUser::getUserDetails).map(UserDetails::getPoints).hasValue(0);
  }

  @Test
  @DisplayName("should return BAD_REQUEST if product is missing")
  void shouldReturnBadRequestIfProductIsMissing() {
    given()
        .contentType(APPLICATION_JSON_VALUE)
        .header(new Header(AUTHORIZATION, BEARER_TOKEN + GOOGLE_PREMIUM_USER.getAccessCode()))
        .param("operation", "EARN")

        .when()
        .post(BASE_PATH + "/user/points")

        .then()
        .log().all(true)
        .status(BAD_REQUEST)
        .body("errors", hasSize(1))
        .body("errors[0]", equalTo("field: `product`, error reason: Product is required"))
    ;
  }
}