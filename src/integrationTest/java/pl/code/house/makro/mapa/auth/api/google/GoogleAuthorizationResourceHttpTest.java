package pl.code.house.makro.mapa.auth.api.google;

import static io.restassured.module.mockmvc.RestAssuredMockMvc.given;
import static org.springframework.http.HttpHeaders.AUTHORIZATION;
import static org.springframework.http.HttpStatus.OK;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static pl.code.house.makro.mapa.auth.api.ApiConstraints.GOOGLE_AUTH_BASE_PATH;

import io.restassured.http.ContentType;
import io.restassured.http.Header;
import io.restassured.module.mockmvc.RestAssuredMockMvc;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.web.context.WebApplicationContext;

@SpringBootTest
class GoogleAuthorizationResourceHttpTest {

  private static final Header VALID_AUTHENTICATION_HEADER = new Header(AUTHORIZATION,
      "Bearer eyJhbGciOiJSUzI1NiIsImtpZCI6ImE0MWEzNTcwYjhlM2FlMWI3MmNhYWJjYWE3YjhkMmRiMjA2NWQ3YzEiLCJ0eXAiOiJKV1QifQ.eyJpc3MiOiJodHRwczovL2FjY291bnRzLmdvb2dsZS5jb20iLCJhenAiOiI1NjQ4MTI2MDYxOTgtN2cxdnRoNHI2OGp1dHNuaDJkMnE4bDBpbWtxaW0wcXYuYXBwcy5nb29nbGV1c2VyY29udGVudC5jb20iLCJhdWQiOiI1NjQ4MTI2MDYxOTgtcTVoZ2prZjQyYWU4ZnE5N3JvMDlrbmo0MmEydDlrY3IuYXBwcy5nb29nbGV1c2VyY29udGVudC5jb20iLCJzdWIiOiIxMTgzNjQ4NDc5MTE1MDIyMTA0MTYiLCJlbWFpbCI6InRlc3QubWFrcm8wMUBnbWFpbC5jb20iLCJlbWFpbF92ZXJpZmllZCI6dHJ1ZSwibmFtZSI6Ik1ha3JvbWFwYSBUZXN0MDEiLCJwaWN0dXJlIjoiaHR0cHM6Ly9saDMuZ29vZ2xldXNlcmNvbnRlbnQuY29tLy1pa1lfQ203MmN6dy9BQUFBQUFBQUFBSS9BQUFBQUFBQUFBQS9BTVp1dWNsT1lzLWxRbXhBV2FoSzROaHFZVS1XMnBjeTdnL3M5Ni1jL3Bob3RvLmpwZyIsImdpdmVuX25hbWUiOiJNYWtyb21hcGEiLCJmYW1pbHlfbmFtZSI6IlRlc3QwMSIsImxvY2FsZSI6InBsIiwiaWF0IjoxNTkzNzIzNDAzLCJleHAiOjE1OTM3MjcwMDN9.eY0qV2DOVfn6EdyvZfAFHXK4hBL_uhSnfPezqWrEuTYmXWYomh9a4yjFFIYCsIv_kqOrlDuSgCLJKnem9DjbBXX8gIbQcqdVemJJmJpGsct05lFmo2S1wR81gFc3Rc5OBQ0_O_plw50r31qmSJtgs_m5G2BTh8HkGJK68BY_EStX2vhhm7vsykC3esDz96yw3QnJdCFi_v-B2ephROhj2QZGNx_5pF0yfHOqslxkwUOYu32TmP-MNgYY0Wlvh9lG7rEYZKV-iyrSdfo3nJKwo7K1zTb0V4rRGM1ByTcVhkDpCLe592bhOhSSeDVt882QpC8AHo1nKWMUv1My54gczw");

  @Autowired
  private WebApplicationContext context;

  @BeforeEach
  void setup() {
    RestAssuredMockMvc.webAppContextSetup(context, springSecurity());
  }

  @Test
  @DisplayName("should correctly convert jwt token to internal access token")
  void shouldCorrectlyConvertJwtTokenToInternalAccessToken() {
    //given
    given()
        .header(VALID_AUTHENTICATION_HEADER)
        .contentType(ContentType.JSON)

        .when()
        .post(GOOGLE_AUTH_BASE_PATH + "/authorize")

        .then()
        .log().ifValidationFails()
        .status(OK)
        .body("accessCode", Matchers.equalTo("sss"))
    ;
  }
}