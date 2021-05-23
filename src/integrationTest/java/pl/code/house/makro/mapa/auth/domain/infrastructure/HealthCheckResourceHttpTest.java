package pl.code.house.makro.mapa.auth.domain.infrastructure;


import static com.github.tomakehurst.wiremock.client.WireMock.okJson;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static io.restassured.http.ContentType.JSON;
import static io.restassured.module.mockmvc.RestAssuredMockMvc.given;
import static io.restassured.module.mockmvc.RestAssuredMockMvc.webAppContextSetup;
import static java.time.Duration.between;
import static java.time.LocalDateTime.now;
import static org.hamcrest.Matchers.equalTo;
import static org.springframework.http.HttpHeaders.AUTHORIZATION;
import static org.springframework.http.HttpStatus.OK;
import static org.springframework.http.HttpStatus.UNAUTHORIZED;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.restassured.http.Header;
import io.vavr.control.Try;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.web.context.WebApplicationContext;

@SpringBootTest
class HealthCheckResourceHttpTest {

  private static final ObjectMapper mapper = new ObjectMapper();
  private static final String USER_SUB = "aa6641c1-e9f4-417f-adf4-f71accc470cb";

  @Value("${info.app.name}")
  String appTitle;

  @Value("${info.app.description}")
  String appDesc;

  @Autowired
  private WebApplicationContext context;

  @BeforeEach
  void setup() {
    webAppContextSetup(context, springSecurity());
  }

  @Test
  @DisplayName("should display health status when user is admin")
  void shouldDisplayHealthStatusWhenUserIsAdmin() {
    //given
    given()
        .auth().with(httpBasic("admin_aga", "mysecretpassword"))

        .contentType(JSON)

        .when()
        .get("/actuator/health")

        .then()
        .log().ifValidationFails()
        .status(OK)
    ;
  }

  @Test
  @DisplayName("should not allow health status when user is not authenticated")
  void shouldNotAllowHealthStatusWhenUserIsNotAuthenticated() {
    //given
    given()
        .contentType(JSON)
        .when()
        .get("/actuator/health")

        .then()
        .status(UNAUTHORIZED)
    ;
  }

  @Test
  @DisplayName("return info status when asking as BasicAuth Admin")
  void returnInfoStatusWhenAskingAsBasicAuthAdmin() {
    //given
    given()
        .auth().with(httpBasic("admin_aga", "mysecretpassword"))

        .contentType(JSON)

        .when()
        .get("/actuator/info")

        .then()
        .log().all(true)
        .status(OK)
        .body("app.name", equalTo(appTitle))
        .body("app.description", equalTo(appDesc))
        .body("build.artifact", equalTo("makromapa-pay"))
        .body("build.name", equalTo("makromapa-pay"))
        .body("build.group", equalTo("pl.code.house.makro.mapa.pay"))
    ;
  }

  @Test
  @DisplayName("should not allow info status when user is not authenticated")
  void shouldNotAllowInfoStatusWhenUserIsNotAuthenticated() {
    //given
    given()
        .contentType(JSON)
        .when()
        .get("/actuator/info")

        .then()
        .status(UNAUTHORIZED)
    ;
  }

  @Test
  @DisplayName("should not allow info status for OAuth2 user2")
  void shouldNotAllowInfoStatusForOAuth2User2() {
    //given
    mockUserInfo();

    given()
        .header(new Header(AUTHORIZATION, "Bearer b146b422-475c-4beb-9e9c-4e33e2288b08"))
        .contentType(JSON)

        .when()
        .get("/actuator/info")

        .then()
        .status(UNAUTHORIZED)
    ;
  }


  static void mockUserInfo() {
    stubFor(post(urlEqualTo("/oauth/check_token"))
        .willReturn(okJson(tokenCheckResponse()))
    );
  }

  private static String tokenCheckResponse() {
    Map<String, Object> tokenResponse = Map.of(
        "active", true,
        "user_name", USER_SUB,
        "aud", List.of("makromapa-mobile"),
        "scope", List.of("USER"),
        "authorities", List.of("ROLE_FREE_USER"),
        "exp", between(now(), now().plusDays(30)).getSeconds()
    );

    return Try.of(() -> mapper.writeValueAsString(tokenResponse))
        .getOrElseThrow((Function<Throwable, RuntimeException>) RuntimeException::new);
  }
}