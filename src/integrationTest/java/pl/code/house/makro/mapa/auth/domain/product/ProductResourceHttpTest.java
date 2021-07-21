package pl.code.house.makro.mapa.auth.domain.product;

import static io.restassured.module.mockmvc.RestAssuredMockMvc.given;
import static io.restassured.module.mockmvc.RestAssuredMockMvc.webAppContextSetup;
import static java.nio.charset.StandardCharsets.UTF_8;
import static java.time.temporal.ChronoUnit.HOURS;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.emptyOrNullString;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.everyItem;
import static org.hamcrest.Matchers.hasItems;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;
import static org.springframework.http.HttpHeaders.AUTHORIZATION;
import static org.springframework.http.HttpHeaders.encodeBasicAuth;
import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.CONFLICT;
import static org.springframework.http.HttpStatus.CREATED;
import static org.springframework.http.HttpStatus.OK;
import static org.springframework.http.HttpStatus.PRECONDITION_REQUIRED;
import static org.springframework.http.MediaType.APPLICATION_FORM_URLENCODED_VALUE;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static pl.code.house.makro.mapa.auth.domain.user.OAuth2Provider.BASIC_AUTH;
import static pl.code.house.makro.mapa.auth.domain.user.OAuth2Provider.GOOGLE;
import static pl.code.house.makro.mapa.auth.domain.user.TestUser.ADMIN;
import static pl.code.house.makro.mapa.auth.domain.user.TestUser.BEARER_TOKEN;
import static pl.code.house.makro.mapa.auth.domain.user.TestUser.GOOGLE_PREMIUM_USER;
import static pl.code.house.makro.mapa.auth.domain.user.TestUser.REG_USER;
import static pl.code.house.makro.mapa.auth.domain.user.UserType.FREE_USER;

import io.restassured.http.Header;
import java.time.Clock;
import java.time.LocalDateTime;
import java.util.UUID;
import org.assertj.core.api.Condition;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.Rollback;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;
import pl.code.house.makro.mapa.auth.domain.user.TestUserAuthoritiesService;
import pl.code.house.makro.mapa.auth.domain.user.TestUserRepository;
import pl.code.house.makro.mapa.auth.domain.user.UserAuthoritiesService;
import pl.code.house.makro.mapa.auth.domain.user.UserQueryFacade;
import pl.code.house.makro.mapa.auth.domain.user.dto.UserInfoDto;

@SpringBootTest
class ProductResourceHttpTest {

  @Autowired
  private WebApplicationContext context;

  @Autowired
  private Clock clock;

  @Autowired
  private UserQueryFacade queryFacade;

  @Autowired
  private TestUserRepository userRepository;

  @Autowired
  private UserAuthoritiesService userAuthoritiesService;

  @Autowired
  private TestUserAuthoritiesService testUserAuthoritiesService;

  @BeforeEach
  void setup() {
    webAppContextSetup(context, springSecurity());
  }

  @Test
  @DisplayName("should return all stored products")
  void shouldReturnAllStoredProducts() {
    given()
        .contentType(APPLICATION_JSON_VALUE)
        .header(new Header(AUTHORIZATION, BEARER_TOKEN + GOOGLE_PREMIUM_USER.getAccessCode()))

        .when()
        .get("/oauth/product")

        .then()
        .log().ifValidationFails()
        .status(OK)

        .body("$", hasSize(4))
        .body("id", everyItem(notNullValue(Integer.class)))
        .body("name", hasItems("APPROVED_DISH_PROPOSAL", "DISABLE_ADS", "sub_premium", "ads_removal"))
        .body("description", everyItem(notNullValue(Integer.class)))
        .body("points", hasItems(0, 100))
        .body("enabled", hasItems(false, true))
        .body("reason", hasItems("USE", "PURCHASE", "EARN"))
    ;
  }

  @Test
  @Rollback
  @Transactional
  @DisplayName("should update user points by earning 100")
  void shouldUpdateUserPointsByEarning100() {
    //given
    int pointsEarned = 100;
    assertThat(queryFacade.findUserById(GOOGLE_PREMIUM_USER.getUserId())).map(UserInfoDto::getPoints).hasValue(0);

    given()
        .contentType(APPLICATION_JSON_VALUE)
        .header(new Header(AUTHORIZATION, BEARER_TOKEN + GOOGLE_PREMIUM_USER.getAccessCode()))
        .param("operation", "EARN")
        .param("product", "1000")

        .when()
        .post("/oauth/product")

        .then()
        .log().all(true)
        .status(CREATED)
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
    assertThat(queryFacade.findUserById(GOOGLE_PREMIUM_USER.getUserId())).map(UserInfoDto::getPoints).hasValue(pointsEarned);
  }

  @Test
  @Rollback
  @Transactional
  @DisplayName("should update other user points by earning 100")
  void shouldUpdateOtherUserPointsByEarning100() {
    //given
    int pointsEarned = 100;

    Header backendAuthHeader = backendAuthHeader();
    assertThat(queryFacade.findUserById(GOOGLE_PREMIUM_USER.getUserId())).map(UserInfoDto::getPoints).hasValue(0);

    given()
        .contentType(APPLICATION_JSON_VALUE)
        .header(backendAuthHeader)
        .param("operation", "EARN")
        .param("product", "1000")

        .when()
        .post("/oauth/product/{userId}", GOOGLE_PREMIUM_USER.getUserId())

        .then()
        .log().ifValidationFails()
        .status(CREATED)
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
    assertThat(queryFacade.findUserById(GOOGLE_PREMIUM_USER.getUserId())).map(UserInfoDto::getPoints).hasValue(pointsEarned);
  }

  @Test
  @Rollback
  @Transactional
  @DisplayName("should handle product purchase that is not expiring")
  void shouldHandleProductPurchaseThatIsNotExpiring() {
    //given
    userRepository.setUserPoints(REG_USER.getUserId(), 500);
    userAuthoritiesService.insertUserAuthorities(REG_USER.getUserId(), FREE_USER);

    assertThat(queryFacade.findUserById(REG_USER.getUserId())).map(UserInfoDto::getPoints).hasValue(500);
    testUserAuthoritiesService.assertUserFeatureRoles(REG_USER.getUserId())
        .hasSize(0);

    given()
        .contentType(APPLICATION_JSON_VALUE)
        .header(userAuthHeader())
        .param("operation", "PURCHASE")
        .param("product", "1002")

        .when()
        .post("/oauth/product")

        .then()
        .log().ifValidationFails()
        .status(CREATED)

        .body("sub", equalTo(REG_USER.getUserId().toString()))
        .body("provider", equalTo(BASIC_AUTH.name()))
        .body("name", is(nullValue()))
        .body("surname", is(nullValue()))
        .body("nickname", is(nullValue()))
        .body("email", equalTo(REG_USER.getName()))
        .body("picture", is(nullValue()))
        .body("type", equalTo("FREE_USER"))
        .body("premiumFeatures", hasItems("DISABLE_ADS"))
        .body("points", equalTo(500))
        .body("enabled", equalTo(true));

    //then
    assertThat(queryFacade.findUserById(REG_USER.getUserId())).map(UserInfoDto::getPoints).hasValue(500);
    testUserAuthoritiesService.assertUserFeatureRoles(REG_USER.getUserId())
        .hasSize(1)
        .filteredOn(tuple -> tuple._1.equalsIgnoreCase("ROLE_DISABLE_ADS"))
        .singleElement()
        .is(new Condition<>(tuple -> LocalDateTime.MAX.equals(tuple._2), "should have life time DISABLE ADS role"));
  }

  @Test
  @Rollback
  @Transactional
  @DisplayName("should handle product ads purchase that will not expire and set same premium feature as per time limited purchase")
  void shouldHandleProductAdsPurchaseThatWillNotExpireAndSetSamePremiumFeatureAsPerTimeLimitedPurchase() {
    //given
    LocalDateTime shouldExpireIn = LocalDateTime.now(clock).plusWeeks(1).plusHours(1).truncatedTo(HOURS);
    userRepository.setUserPoints(REG_USER.getUserId(), 500);
    userAuthoritiesService.insertUserAuthorities(REG_USER.getUserId(), FREE_USER);

    assertThat(queryFacade.findUserById(REG_USER.getUserId()))
        .map(UserInfoDto::getPoints)
        .hasValue(500);
    testUserAuthoritiesService.assertUserFeatureRoles(REG_USER.getUserId())
        .hasSize(0);

    given()
        .contentType(APPLICATION_JSON_VALUE)
        .header(userAuthHeader())
        .param("operation", "USE")
        .param("product", "1003")

        .when()
        .post("/oauth/product")

        .then()
        .log().ifValidationFails()
        .status(CREATED)

        .body("sub", equalTo(REG_USER.getUserId().toString()))
        .body("provider", equalTo(BASIC_AUTH.name()))
        .body("name", is(nullValue()))
        .body("surname", is(nullValue()))
        .body("nickname", is(nullValue()))
        .body("email", equalTo(REG_USER.getName()))
        .body("picture", is(nullValue()))
        .body("type", equalTo("FREE_USER"))
        .body("premiumFeatures", hasItems("DISABLE_ADS"))
        .body("points", equalTo(100))
        .body("enabled", equalTo(true));

    //then
    assertThat(queryFacade.findUserById(REG_USER.getUserId())).map(UserInfoDto::getPoints).hasValue(100);
    testUserAuthoritiesService.assertUserFeatureRoles(REG_USER.getUserId())
        .hasSize(1)
        .filteredOn(tuple -> tuple._1.equalsIgnoreCase("ROLE_DISABLE_ADS"))
        .singleElement()
        .is(new Condition<>(tuple -> shouldExpireIn.equals(tuple._2), "should have life time DISABLE ADS role"));
  }

  @Test
  @Rollback
  @Transactional
  @DisplayName("should handle product purchase that is expiring in given time")
  void shouldHandleProductPurchaseThatIsExpiringInGivenTime() {
    //given
    LocalDateTime shouldExpireIn = LocalDateTime.now(clock).plusWeeks(4).plusHours(1).truncatedTo(HOURS);
    userRepository.setUserPoints(REG_USER.getUserId(), 500);
    userAuthoritiesService.insertUserAuthorities(REG_USER.getUserId(), FREE_USER);

    assertThat(queryFacade.findUserById(REG_USER.getUserId())).map(UserInfoDto::getPoints).hasValue(500);
    testUserAuthoritiesService.assertUserFeatureRoles(REG_USER.getUserId())
        .hasSize(0);

    given()
        .contentType(APPLICATION_JSON_VALUE)
        .header(userAuthHeader())
        .param("operation", "PURCHASE")
        .param("product", "1001")

        .when()
        .post("/oauth/product")

        .then()
        .log().ifValidationFails()
        .status(CREATED)

        .body("sub", equalTo(REG_USER.getUserId().toString()))
        .body("provider", equalTo(BASIC_AUTH.name()))
        .body("name", is(nullValue()))
        .body("surname", is(nullValue()))
        .body("nickname", is(nullValue()))
        .body("email", equalTo(REG_USER.getName()))
        .body("picture", is(nullValue()))
        .body("type", equalTo("FREE_USER"))
        .body("premiumFeatures", hasItems("PREMIUM"))
        .body("points", equalTo(500))
        .body("enabled", equalTo(true));

    //then
    assertThat(queryFacade.findUserById(REG_USER.getUserId())).map(UserInfoDto::getPoints).hasValue(500);
    testUserAuthoritiesService.assertUserFeatureRoles(REG_USER.getUserId())
        .hasSize(1)
        .filteredOn(tuple -> tuple._1.equalsIgnoreCase("ROLE_PREMIUM"))
        .singleElement()
        .is(new Condition<>(tuple -> shouldExpireIn.equals(tuple._2), "should have life time PREMIUM role"));
  }

  @Test
  @Rollback
  @Transactional
  @DisplayName("should skip if request was send for admin user")
  void shouldSkipIfRequestWasSendForAdminUser() {
    //given
    Header backendAuthHeader = backendAuthHeader();
    assertThat(queryFacade.findUserById(ADMIN.getUserId())).map(UserInfoDto::getPoints).hasValue(0);

    given()
        .contentType(APPLICATION_JSON_VALUE)
        .header(backendAuthHeader)
        .param("operation", "EARN")
        .param("product", "1000")

        .when()
        .post("/oauth/product/{userId}", ADMIN.getUserId())

        .then()
        .log().ifValidationFails()
        .status(CREATED)
        .body("sub", equalTo(ADMIN.getUserId().toString()))
        .body("provider", equalTo(BASIC_AUTH.name()))
        .body("name", is(emptyOrNullString()))
        .body("surname", is(emptyOrNullString()))
        .body("nickname", is(emptyOrNullString()))
        .body("email", is(ADMIN.getName()))
        .body("picture", is(emptyOrNullString()))
        .body("type", equalTo("ADMIN_USER"))
        .body("premiumFeatures", empty())
        .body("points", equalTo(0))
        .body("enabled", equalTo(true));

    //then
    assertThat(queryFacade.findUserById(ADMIN.getUserId())).map(UserInfoDto::getPoints).hasValue(0);
  }

  @Test
  @DisplayName("should return CONFLICT when using invalid operation with given product")
  void shouldReturnConflictWhenUsingInvalidOperationWithGivenProduct() {
    given()
        .contentType(APPLICATION_JSON_VALUE)
        .header(new Header(AUTHORIZATION, BEARER_TOKEN + GOOGLE_PREMIUM_USER.getAccessCode()))
        .param("operation", "EARN")
        .param("product", "1003")

        .when()
        .post("/oauth/product")

        .then()
        .log().all(true)
        .status(CONFLICT)
        .body("error", equalTo("Product `DISABLE_ADS` does not accept following operation reason to assign points to user:EARN"));

    //then
    assertThat(queryFacade.findUserById(GOOGLE_PREMIUM_USER.getUserId())).map(UserInfoDto::getPoints).hasValue(0);
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
        .post("/oauth/product")

        .then()
        .log().all(true)
        .status(BAD_REQUEST)
        .body("error", equalTo("Could not find product by id [1200] that user want's to purchase"));

    //then
    assertThat(queryFacade.findUserById(GOOGLE_PREMIUM_USER.getUserId())).map(UserInfoDto::getPoints).hasValue(0);
  }

  @Test
  @DisplayName("should return BAD_REQUEST if product is missing")
  void shouldReturnBadRequestIfProductIsMissing() {
    given()
        .contentType(APPLICATION_JSON_VALUE)
        .header(new Header(AUTHORIZATION, BEARER_TOKEN + GOOGLE_PREMIUM_USER.getAccessCode()))
        .param("operation", "EARN")

        .when()
        .post("/oauth/product")

        .then()
        .log().all(true)
        .status(BAD_REQUEST)
        .body("errors", hasSize(1))
        .body("errors[0]", equalTo("field: `product`, error reason: Product is required"))
    ;
  }

  @Test
  @Transactional
  @DisplayName("should return PRE_CONDITION when user does not have enough points to use for product")
  void shouldReturnPreConditionWhenUserDoesNotHaveEnoughPointsToUseForProduct() {
    //given
    userRepository.setUserPoints(REG_USER.getUserId(), 200);
    userAuthoritiesService.insertUserAuthorities(REG_USER.getUserId(), FREE_USER);

    assertThat(queryFacade.findUserById(REG_USER.getUserId()))
        .map(UserInfoDto::getPoints)
        .hasValue(200);
    testUserAuthoritiesService.assertUserFeatureRoles(REG_USER.getUserId())
        .hasSize(0);

    given()
        .contentType(APPLICATION_JSON_VALUE)
        .header(userAuthHeader())
        .param("operation", "USE")
        .param("product", "1003")

        .when()
        .post("/oauth/product")

        .then()
        .log().ifValidationFails()
        .status(PRECONDITION_REQUIRED)
        .body("uniqueErrorId", notNullValue(UUID.class))
        .body("error", equalTo("User does not have enough points to use them on product: `DISABLE_ADS` (required minimum points: 400)"))
    ;

    //then
    assertThat(queryFacade.findUserById(REG_USER.getUserId()))
        .map(UserInfoDto::getPoints)
        .hasValue(200);
    testUserAuthoritiesService.assertUserFeatureRoles(REG_USER.getUserId())
        .hasSize(0);
  }

  private Header backendAuthHeader() {
    String accessToken = given()
        .param("grant_type", "client_credentials")
        .param("clientId", "makromapa-backend")
        .param("clientSecret", "secret")
        .header(new Header(AUTHORIZATION, "Basic " + encodeBasicAuth("makromapa-backend", "secret", UTF_8)))

        .contentType(APPLICATION_FORM_URLENCODED_VALUE)

        .when()
        .post("/oauth/token")

        .then()
        .status(OK)
        .log().ifValidationFails()
        .extract().body().jsonPath().getString("access_token");

    return new Header(AUTHORIZATION, BEARER_TOKEN + accessToken);
  }

  private Header userAuthHeader() {
    String accessToken = given()
        .param("grant_type", "password")
        .param("username", REG_USER.getName())
        .param("password", REG_USER.getPassword())
        .header(new Header(AUTHORIZATION, "Basic " + encodeBasicAuth("basic-auth-makromapa-mobile", "secret", UTF_8)))
        .contentType(APPLICATION_FORM_URLENCODED_VALUE)

        .when()
        .post("/oauth/token")

        .then()
        .status(OK)
        .log().ifValidationFails()
        .extract().body().jsonPath().getString("access_token");

    return new Header(AUTHORIZATION, BEARER_TOKEN + accessToken);
  }
}