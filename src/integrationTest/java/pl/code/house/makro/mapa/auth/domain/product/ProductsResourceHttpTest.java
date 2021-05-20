package pl.code.house.makro.mapa.auth.domain.product;

import static io.restassured.module.mockmvc.RestAssuredMockMvc.given;
import static io.restassured.module.mockmvc.RestAssuredMockMvc.webAppContextSetup;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasItems;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.notNullValue;
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
import org.springframework.web.context.WebApplicationContext;

@SpringBootTest
class ProductsResourceHttpTest {

  @Autowired
  private WebApplicationContext context;

  @BeforeEach
  void setup() {
    webAppContextSetup(context, springSecurity());
  }

  @Test
  @DisplayName("should return all known point products")
  void shouldReturnAllKnownPointProducts() {
    given()
        .contentType(APPLICATION_JSON_VALUE)
        .header(new Header(AUTHORIZATION, BEARER_TOKEN + GOOGLE_PREMIUM_USER.getAccessCode()))
        .param("operation", "EARN")

        .when()
        .get(BASE_PATH + "/product")

        .then()
        .log().ifValidationFails()
        .status(OK)
        .body("$", hasSize(9))
        .body("id", notNullValue(Long.class))
        .body("name", hasItems(
            equalTo("APPROVED_DISH_PROPOSAL"),
            equalTo("PURCHASE_100"),
            equalTo("PURCHASE_500"),
            equalTo("PURCHASE_1000"),
            equalTo("PURCHASE_500"),
            equalTo("USE_ADS_WEEK"),
            equalTo("USE_ADS_MONTH"),
            equalTo("USE_PREMIUM_WEEK")
        ))
        .body("description", notNullValue(String.class))
        .body("points", notNullValue(Integer.class))
        .body("reasons.flatten()", hasItems("EARN", "PURCHASE", "USE"))
    ;
  }
}