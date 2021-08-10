package pl.code.house.makro.mapa.auth.domain.product;

import static io.restassured.module.mockmvc.RestAssuredMockMvc.given;
import static io.restassured.module.mockmvc.RestAssuredMockMvc.webAppContextSetup;
import static org.springframework.http.HttpHeaders.AUTHORIZATION;
import static org.springframework.http.HttpStatus.OK;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static pl.code.house.makro.mapa.auth.domain.user.TestUser.BEARER_TOKEN;
import static pl.code.house.makro.mapa.auth.domain.user.TestUser.GOOGLE_PREMIUM_USER;

import io.restassured.http.Header;
import java.time.Clock;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.web.context.WebApplicationContext;

@SpringBootTest
class ReceiptValidationResourceHttpTest {

  private static final String PRODUCTION_RECEIPT = "MIJF9wYJKoZIhvcNAQcCoIJF6DCCReQCAQExCzAJBgUrDgMCGgUAMII1mAYJKoZIhvcNAQcBoII1iQSCNYUxgjWBMAoCAQgCAQEEAhYAMAoCARQCAQEEAgwAMAsCAQECAQEEAwIBADALAgELAgEBBAMCAQAwCwIBDwIBAQQDAgEAMAsCARACAQEEAwIBADALAgEZAgEBBAMCAQMwDAIBAwIBAQQEDAIyNTAMAgEKAgEBBAQWAjQrMAwCAQ4CAQEEBAICAM8wDQIBDQIBAQQFAgMCIuAwDQIBEwIBAQQFDAMxLjAwDgIBCQIBAQQGAgRQMjU1MBgCAQQCAQIEEBE8i4A67jeWmcZBUG0nziswGwIBAAIBAQQTDBFQcm9kdWN0aW9uU2FuZGJveDAcAgEFAgEBBBRXmxfahuClkpJtx6E9Xn";
  private static final String SANDBOX_RECEIPT = "ewoJInNpZ25hdHVyZSIgPSAiQW5USlN6UUFqZWhXWW1ucWxvZk9ZVnFyWEo1MVVOWnI5Ly8ySFhxM01COWkyYVBqVmlsdjM4aXhtWm9PLzlZZlBsUkhZRHVzWFQySXBZYkRzNHBGWk53L21RTDFUemtJSWV0WWVhNE95anVWNUtsdUVCNExLVm9sN25tSGZkMjdISTZQTTZqQkRaS0xtcGt0bU5WQ21mbmhlVCtqbE1qTHg3ZVpLakhTRmhsUkFBQURWekNDQTFNd2dnSTdvQU1DQVFJQ0NCdXA0K1BBaG0vTE1BMEdDU3FHU0liM0RRRUJCUVVBTUg4eEN6QUpCZ05WQkFZVEFsVlRNUk13RVFZRFZRUUtEQXBCY0hCc1pTQkpibU11TVNZd0pBWURWUVFMREIxQmNIQnNaU0JEWlhKMGFXWnBZMkYwYVc5dUlFRjFkR2h2Y21sMGVURXpNREVHQTFVRUF3d3FRWEJ3YkdVZ2FWUjFibVZ6SUZOMGIzSmxJRU5sY25ScFptbGpZWFJwYjI0Z1FYVjBhRzl5YVhSNU1CNFhEVEUwTURZd056QXdNREl5TVZvWERURTJNRFV4T0RFNE16RXpNRm93WkRFak1DRUdBMVVFQXd3YVVIVnlZMmhoYzJWU1pXTmxhWEIwUTJWeWRHbG1hV05oZEdVeEd6QVpCZ05WQkFzTUVrRndjR3hsSUdsVWRXNWxjeUJUZEc5eVpURVRNQkVHQTFVRUNnd0tRWEJ3YkdVZ1NXNWpMakVMTUFrR0ExVUVCaE1DVlZNd2daOHdEUVlKS29aSWh2Y05BUUVCQlFBRGdZMEFNSUdKQW9HQkFNbVRFdUxnamltTHdSSnh5MW9FZjBlc1VORFZFSWU2d0Rzbm5hbDE0aE5CdDF2MTk1WDZuOTNZTzdnaTNvclBTdXg5RDU1NFNrTXArU2F5Zzg0bFRjMzYyVXRtWUxwV25iMzRucXlHeDlLQlZUeTVPR1Y0bGpFMU93QytvVG5STStRTFJDbWVOeE1iUFpoUzQ3VCtlWnRERWhWQjl1c2szK0pNMkNvZ2Z3bzdBZ01CQUFHamNqQndNQjBHQTFVZERnUVdCQlNKYUVlTnVxOURmNlpmTjY4RmUrSTJ1MjJzc0RBTUJnTlZIUk1CQWY4RUFqQUFNQjhHQTFVZEl3UVlNQmFBRkRZZDZPS2RndElCR0xVeWF3N1hRd3VSV0VNNk1BNEdBMVVkRHdFQi93UUVBd0lIZ0RBUUJnb3Foa2lHOTJOa0JnVUJCQUlGQURBTkJna3Foa2lHOXcwQkFRVUZBQU9DQVFFQWVhSlYyVTUxcnhmY3FBQWU1QzIvZkVXOEtVbDRpTzRsTXV0YTdONlh6UDFwWkl6MU5ra0N0SUl3ZXlOajVVUllISytIalJLU1U5UkxndU5sMG5rZnhxT2JpTWNrd1J1ZEtTcTY5Tkluclp5Q0Q2NlI0Szc3bmI5bE1UQUJTU1lsc0t0OG9OdGxoZ1IvMWtqU1NSUWNIa3RzRGNTaVFHS01ka1NscDRBeVhmN3ZuSFBCZTR5Q3dZVjJQcFNOMDRrYm9pSjNwQmx4c0d3Vi9abEwyNk0ydWVZSEtZQ3VYaGRxRnd4VmdtNTJoM29lSk9PdC92WTRFY1FxN2VxSG02bTAzWjliN1BSellNMktHWEhEbU9Nazd2RHBlTVZsTERQU0dZejErVTNzRHhKemViU3BiYUptVDdpbXpVS2ZnZ0VZN3h4ZjRjemZIMHlqNXdOelNHVE92UT09IjsKCSJwdXJjaGFzZS1pbmZvIiA9ICJld29KSW05eWFXZHBibUZzTFhCMWNtTm9ZWE5sTFdSaGRHVXRjSE4wSWlBOUlDSXlNREUxTFRFeExURTNJREE0T2pNME9qUXhJRUZ0WlhKcFkyRXZURzl6WDBGdVoyVnNaWE1pT3dvSkluQjFjbU5vWVhObExXUmhkR1V0YlhNaUlEMGdJakUwTkRjNE1qRXlPREV3TURBaU93b0pJblZ1YVhGMVpTMXBaR1Z1ZEdsbWFXVnlJaUE5SUNKa1pEQmxNek5sWW1ZMU5tSmlNV1l6WVdRMFl6SmtZbUZoTjJFd1lqTXpZV00yT0dJd1pUZzFJanNLQ1NKdmNtbG5hVzVoYkMxMGNtRnVjMkZqZEdsdmJpMXBaQ0lnUFNBaU1UQXdNREF3TURFNE1EWXpOakl5TmlJN0Nna2laWGh3YVhKbGN5MWtZWFJsSWlBOUlDSXhORFE0TXpRMk9EZ3hNREF3SWpzS0NTSjBjbUZ1YzJGamRHbHZiaTFwWkNJZ1BTQWlNVEF3TURBd01ERTRNVEk1T0Rjek5DSTdDZ2tpYjNKcFoybHVZV3d0Y0hWeVkyaGhjMlV0WkdGMFpTMXRjeUlnUFNBaU1UUTBOemMzT0RBNE1UQXdNQ0k3Q2draWQyVmlMVzl5WkdWeUxXeHBibVV0YVhSbGJTMXBaQ0lnUFNBaU1UQXdNREF3TURBek1Ea3pPRGN4TXlJN0Nna2lZblp5Y3lJZ1BTQWlNU0k3Q2draWRXNXBjWFZsTFhabGJtUnZjaTFwWkdWdWRHbG1hV1Z5SWlBOUlDSTBSVFZGTVVFelFpMUdNREZCTFRRNE5UVXRPREl3UmkxR016UTVSakV5TkRJeE5EZ2lPd29KSW1WNGNHbHlaWE10WkdGMFpTMW1iM0p0WVhSMFpXUXRjSE4wSWlBOUlDSXlNREUxTFRFeExUSXpJREl5T2pNME9qUXhJRUZ0WlhKcFkyRXZURzl6WDBGdVoyVnNaWE1pT3dvSkltbDBaVzB0YVdRaUlEMGdJakV3TWpnNU5UQTNPVGNpT3dvSkltVjRjR2x5WlhNdFpHRjBaUzFtYjNKdFlYUjBaV1FpSUQwZ0lqSXdNVFV0TVRFdE1qUWdNRFk2TXpRNk5ERWdSWFJqTDBkTlZDSTdDZ2tpY0hKdlpIVmpkQzFwWkNJZ1BTQWllV1ZoY214NUlqc0tDU0p3ZFhKamFHRnpaUzFrWVhSbElpQTlJQ0l5TURFMUxURXhMVEU0SURBME9qTTBPalF4SUVWMFl5OUhUVlFpT3dvSkltOXlhV2RwYm1Gc0xYQjFjbU5vWVhObExXUmhkR1VpSUQwZ0lqSXdNVFV0TVRFdE1UY2dNVFk2TXpRNk5ERWdSWFJqTDBkTlZDSTdDZ2tpWW1sa0lpQTlJQ0pqYjIwdWJXSmhZWE41TG1sdmN5NWtaVzF2SWpzS0NTSndkWEpqYUdGelpTMWtZWFJsTFhCemRDSWdQU0FpTWpBeE5TMHhNUzB4TnlBeU1Eb3pORG8wTVNCQmJXVnlhV05oTDB4dmMxOUJibWRsYkdWeklqc0tDU0p4ZFdGdWRHbDBlU0lnUFNBaU1TSTdDbjA9IjsKCSJlbnZpcm9ubWVudCIgPSAiU2FuZGJveCI7CgkicG9kIiA9ICIxMDAiOwoJInNpZ25pbmctc3RhdHVzIiA9ICIwIjsKfQ==";

  @Autowired
  private WebApplicationContext context;

  @Autowired
  private Clock clock;

  @BeforeEach
  void setup() {
    webAppContextSetup(context, springSecurity());
  }

  @Test
  @DisplayName("should correctly validate apple receipt")
  void shouldCorrectlyValidateAppleReceipt() {
    Map<Object, Object> payload = Map.of(
        "store", "APP_STORE",
        "localReceipt", PRODUCTION_RECEIPT
    );

    given()
        .contentType(APPLICATION_JSON_VALUE)
        .header(new Header(AUTHORIZATION, BEARER_TOKEN + GOOGLE_PREMIUM_USER.getAccessCode()))
        .body(payload)

        .when()
        .post("/oauth/receipt")

        .then()
        .log().ifValidationFails()
        .status(OK)
    ;
  }

  @Test
  @DisplayName("should correctly validate sandbox receipt")
  void shouldCorrectlyValidateSandboxReceipt() {
    Map<Object, Object> payload = Map.of(
        "store", "APP_STORE",
        "localReceipt", SANDBOX_RECEIPT
    );

    given()
        .contentType(APPLICATION_JSON_VALUE)
        .header(new Header(AUTHORIZATION, BEARER_TOKEN + GOOGLE_PREMIUM_USER.getAccessCode()))
        .body(payload)

        .when()
        .post("/oauth/receipt")

        .then()
        .log().ifValidationFails()
        .status(OK)
    ;
  }

}