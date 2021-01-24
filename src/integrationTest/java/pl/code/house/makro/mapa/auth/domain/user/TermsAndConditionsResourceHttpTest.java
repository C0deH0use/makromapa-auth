package pl.code.house.makro.mapa.auth.domain.user;

import static io.restassured.module.mockmvc.RestAssuredMockMvc.given;
import static io.restassured.module.mockmvc.RestAssuredMockMvc.webAppContextSetup;
import static java.nio.charset.StandardCharsets.UTF_8;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;
import static org.springframework.http.HttpHeaders.AUTHORIZATION;
import static org.springframework.http.HttpHeaders.encodeBasicAuth;
import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.OK;
import static org.springframework.http.MediaType.APPLICATION_FORM_URLENCODED_VALUE;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static pl.code.house.makro.mapa.auth.ApiConstraints.BASE_PATH;
import static pl.code.house.makro.mapa.auth.domain.user.TestUser.BEARER_TOKEN;
import static pl.code.house.makro.mapa.auth.domain.user.TestUser.GOOGLE_PREMIUM_USER;
import static pl.code.house.makro.mapa.auth.domain.user.TestUser.REG_USER;

import io.restassured.http.Header;
import java.util.UUID;
import org.assertj.core.api.OptionalAssert;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.Rollback;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;

@SpringBootTest
class TermsAndConditionsResourceHttpTest {

  @Autowired
  private WebApplicationContext context;

  @Autowired
  private TestUserRepository userRepository;

  @BeforeEach
  void setup() {
    webAppContextSetup(context, springSecurity());
  }

  @Test
  @DisplayName("should fetch latest Terms And Condition dto")
  void shouldFetchLatestTermsAndConditionDto() {
    //given
    given()
        .contentType(APPLICATION_JSON_VALUE)
        .header(AUTHORIZATION, BEARER_TOKEN + GOOGLE_PREMIUM_USER.getAccessCode())

        .when()
        .get(BASE_PATH + "/terms-and-conditions")

        .then()
        .log().ifValidationFails()
        .status(OK)
        .body("id", equalTo(1001))
        .body("contractPl", equalTo("TESTOWY_REGULAMIN_2"))
        .body("contractEn", equalTo("TEST_TERMS_CONDITIONS_2"))
    ;
  }

  @Test
  @Rollback
  @Transactional
  @DisplayName("should successfully update user terms id when approving valid terms and conditions")
  void shouldSuccessfullyUpdateUserTermsIdWhenApprovingValidTermsAndConditions() {
    //given
    userRepository.updateUserTermsAndConditionsId(GOOGLE_PREMIUM_USER.getUserId(), null);
    assertApprovedTermsForUser(GOOGLE_PREMIUM_USER.getUserId()).isEmpty();

    given()
        .contentType(APPLICATION_JSON_VALUE)
        .header(AUTHORIZATION, BEARER_TOKEN + GOOGLE_PREMIUM_USER.getAccessCode())

        .when()
        .post(BASE_PATH + "/terms-and-conditions/1001/approve")

        .then()
        .log().ifValidationFails()
        .status(OK)
        .body("id", equalTo(1001))
        .body("contractPl", equalTo("TESTOWY_REGULAMIN_2"))
        .body("contractEn", equalTo("TEST_TERMS_CONDITIONS_2"))
    ;

    assertApprovedTermsForUser(GOOGLE_PREMIUM_USER.getUserId()).hasValue(1001L);
  }

  @Test
  @Rollback
  @Transactional
  @DisplayName("should store approved Terms for Basic auth user")
  void shouldStoreApprovedTermsForBasicAuthUser() {
    //given
    assertApprovedTermsForUser(REG_USER.getUserId()).isEmpty();

    String accessCode = given()
        .param("grant_type", "password")
        .param("username", REG_USER.getName())
        .param("password", REG_USER.getPassword())
        .header(new Header(AUTHORIZATION, "Basic " + encodeBasicAuth("basic-auth-makromapa-mobile", "secret", UTF_8)))
        .contentType(APPLICATION_FORM_URLENCODED_VALUE)
        .when()
        .post("/oauth/token")
        .then()
        .status(OK)
        .extract().body().jsonPath().getString("access_token");

    given()
        .contentType(APPLICATION_JSON_VALUE)
        .header(AUTHORIZATION, BEARER_TOKEN + accessCode)

        .when()
        .post(BASE_PATH + "/terms-and-conditions/1001/approve")

        .then()
        .log().ifValidationFails()
        .status(OK)
        .body("id", equalTo(1001))
        .body("contractPl", equalTo("TESTOWY_REGULAMIN_2"))
        .body("contractEn", equalTo("TEST_TERMS_CONDITIONS_2"))
    ;

    assertApprovedTermsForUser(REG_USER.getUserId()).hasValue(1001L);
  }

  @Test
  @DisplayName("should return BAD_REQUEST when user approving invalid terms and condition record")
  void shouldReturnBadRequestWhenUserApprovingInvalidTermsAndConditionRecord() {
    //given
    given()
        .contentType(APPLICATION_JSON_VALUE)
        .header(AUTHORIZATION, BEARER_TOKEN + GOOGLE_PREMIUM_USER.getAccessCode())

        .when()
        .post(BASE_PATH + "/terms-and-conditions/1000/approve")

        .then()
        .log().ifValidationFails()
        .status(BAD_REQUEST)
        .body("error", equalTo(""))
    ;
  }

  private OptionalAssert<Long> assertApprovedTermsForUser(UUID userId) {
    return assertThat(userRepository.findById(userId).map(BaseUser::getTermsAndConditionsId));
  }
}