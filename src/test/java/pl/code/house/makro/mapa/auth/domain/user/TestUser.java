package pl.code.house.makro.mapa.auth.domain.user;

import static java.util.UUID.fromString;
import static org.springframework.http.HttpHeaders.AUTHORIZATION;

import io.restassured.http.Header;
import java.util.UUID;
import lombok.EqualsAndHashCode;
import lombok.Value;
import lombok.experimental.NonFinal;

public class TestUser {

  public static final String BEARER_TOKEN = "Bearer ";

  //codehouse.makromapa@gmail.com
  public static final String NEW_USER_TOKEN = "eyJhbGciOiJSUzI1NiIsImtpZCI6Ijc0NGY2MGU5ZmI1MTVhMmEwMWMxMWViZWIyMjg3MTI4NjA1NDA3MTEiLCJ0eXAiOiJKV1QifQ.eyJpc3MiOiJodHRwczovL2FjY291bnRzLmdvb2dsZS5jb20iLCJhenAiOiI1NjQ4MTI2MDYxOTgtcHNqbzAwamJ1bWFzMTE3cGV1YjVoMzN2ODN0NGozaXAuYXBwcy5nb29nbGV1c2VyY29udGVudC5jb20iLCJhdWQiOiI1NjQ4MTI2MDYxOTgtcHNqbzAwamJ1bWFzMTE3cGV1YjVoMzN2ODN0NGozaXAuYXBwcy5nb29nbGV1c2VyY29udGVudC5jb20iLCJzdWIiOiIxMDQ3OTY4NjU4ODE0ODEwMzQzMDAiLCJlbWFpbCI6ImNvZGVob3VzZS5tYWtyb21hcGFAZ21haWwuY29tIiwiZW1haWxfdmVyaWZpZWQiOnRydWUsImF0X2hhc2giOiJTOW1uZ3N3QVlkSDQwa0xuUXhaekVBIiwibm9uY2UiOiJmN3ZSdk1UYy1kRURLVzZ1bGkzaVFhdDJfMnJ6VjZFQk5mNE8xdHY3NjlRIiwibmFtZSI6Ik1ha3JvIE1hcGEiLCJwaWN0dXJlIjoiaHR0cHM6Ly9saDMuZ29vZ2xldXNlcmNvbnRlbnQuY29tL2EtL0FPaDE0R2p4elJMaUZDT0dMZ0JqV2dqX2ZsM2QzZFdKNnE5Ny1vZXR2WXRoPXM5Ni1jIiwiZ2l2ZW5fbmFtZSI6Ik1ha3JvIiwiZmFtaWx5X25hbWUiOiJNYXBhIiwibG9jYWxlIjoicGwiLCJpYXQiOjE1OTcwODk1NjgsImV4cCI6MTU5NzA5MzE2OH0.ZAZslARyfWRtuTVPFF0K7JPGeTw8jEvMTlPcGg30DT8Mnx2t4FtgEEkFK--0oKtnKiUIbeEXxThWWjO0IyGpwNMh5bzQ6PnoQ2KMbOt4wGNgXHchTNNbYzvEK194n1EQiqvPNik4ayDQRg6cS6PUjqYoRn9WABKazG6z8WdImri2FxQtpQ6vbtAwIr_gdwgLmD4jXZxFLJAx2T-nplNTPwEed-ovisRRc-_Hx3JZNW0G2pfqn7H8Wnmld95nUbJMQWfk9gcNmY5pQcpFD--kZncrRe3HBEOumLmpFqoMFD34lsHMRgjQZCMqsl-YrcRwX6e2z62S7SOA35A9COTLqg";

  //test.makro01@gmail.com
  public static final String PREMIUM_USER_TOKEN = "eyJhbGciOiJSUzI1NiIsImtpZCI6ImYwNTQxNWIxM2FjYjk1OTBmNzBkZjg2Mjc2NWM2NTVmNWE3YTAxOWUiLCJ0eXAiOiJKV1QifQ.eyJpc3MiOiJodHRwczovL2FjY291bnRzLmdvb2dsZS5jb20iLCJhenAiOiI1NjQ4MTI2MDYxOTgtcHNqbzAwamJ1bWFzMTE3cGV1YjVoMzN2ODN0NGozaXAuYXBwcy5nb29nbGV1c2VyY29udGVudC5jb20iLCJhdWQiOiI1NjQ4MTI2MDYxOTgtcHNqbzAwamJ1bWFzMTE3cGV1YjVoMzN2ODN0NGozaXAuYXBwcy5nb29nbGV1c2VyY29udGVudC5jb20iLCJzdWIiOiIxMTgzNjQ4NDc5MTE1MDIyMTA0MTYiLCJlbWFpbCI6InRlc3QubWFrcm8wMUBnbWFpbC5jb20iLCJlbWFpbF92ZXJpZmllZCI6dHJ1ZSwiYXRfaGFzaCI6IlNtY0dHM3RPVjRnQm9hWVNLRnBsVUEiLCJub25jZSI6InVTeU9vTU9oc002ZTEwS1lkOWxIcU1jOExZRjNzaWFNbkcyMkRFdms0OEEiLCJuYW1lIjoiTWFrcm9tYXBhIFRlc3QwMSIsInBpY3R1cmUiOiJodHRwczovL2xoMy5nb29nbGV1c2VyY29udGVudC5jb20vLWlrWV9DbTcyY3p3L0FBQUFBQUFBQUFJL0FBQUFBQUFBQUFBL0FNWnV1Y2xPWXMtbFFteEFXYWhLNE5ocVlVLVcycGN5N2cvczk2LWMvcGhvdG8uanBnIiwiZ2l2ZW5fbmFtZSI6Ik1ha3JvbWFwYSIsImZhbWlseV9uYW1lIjoiVGVzdDAxIiwibG9jYWxlIjoicGwiLCJpYXQiOjE1OTY2OTYwMjYsImV4cCI6MTU5NjY5OTYyNn0.ISHTKuBnzdtAB5e3TYyJ8wQ6kyTHDuCNFqxVoo7lULpsGs_jeYI2okGnxsWfrD0xVNH5-D9f-05YyFWKP1NgVBwLX1YQm1LkH8ZtOSQcsfvhOuhR8yTD1nhKzWK5WYob8eL1n8j7Ba23qwqMI986YKPUg5kv5jkslqpelXizjqbnqApUlCUNcaAvD8v_c8QwUopo-GnsjAJaQUYiKe-XKYz4H6TQyqo0kv7n5GpBqry-7bkxCuNFPfXM4xAdGUxZ7m_xHlgqj7mzqPm2Ev64oTMzhh9gAqhZcVmcHOc-7_4ATdjEeqzpv5ow7lEIh19lL9RL9PO3ku-AynTVPFxioQ";

  //Marek.Malik@itds.pl
  public static final String APPLE_USER_TOKEN = "eyJraWQiOiJlWGF1bm1MIiwiYWxnIjoiUlMyNTYifQ.eyJpc3MiOiJodHRwczovL2FwcGxlaWQuYXBwbGUuY29tIiwiYXVkIjoicGwuY29kZWhvdXNlLm1ha3JvLm1hcGEubW9iaWxlIiwiZXhwIjoxNTk2MzI0Njg2LCJpYXQiOjE1OTYzMjQwODYsInN1YiI6IjAwMDgwNy40ZjkxMWNkNmUwM2E0ZWJiOTMzYzRlMDk5YTdhZDcxMC4yMTU4IiwiY19oYXNoIjoiMWpCQ2c5Yk1oMHVSSUM1VVJXSXNpQSIsImF1dGhfdGltZSI6MTU5NjMyNDA4Niwibm9uY2Vfc3VwcG9ydGVkIjp0cnVlfQ.AA9MFudjOl0HN7HP9o63AGFEzzcWnlTixfdeZU0tyYFgVITB2P_QDwcvnD5f_V3kQAx6iF6ErGbQGtTTBRRzFHOBFnGXKACEgUeq1soOrRjKX9A63Vc1GZ5RhwjHacO_qFXidIsAk2k7Te3AXfi7SbH_DGk1-NMve2GQnDSroH7biUv6CdEoRJwG3GLeelBxyf8qXBJM3VWaBg71KnoHHiAQ-QSnZiie8-Euk8oYJObcTqFPLzib0ZZp8yifm2fTZcCsXP8mVoM2CoIGKNcHkg3yfqVhsvX-1O3Uns4SdMzapo0CSDJdWklc8F7E-NMjlPiK9gyOR5EGbaMahkEnXA";

  // -- External(JWT) UserDetails
  public static ExternalMockUser GOOGLE_NEW_USER = new ExternalMockUser(null, "Makro Mapa", "104796865881481034300", NEW_USER_TOKEN, null);
  public static ExternalMockUser GOOGLE_PREMIUM_USER = new ExternalMockUser("aa6641c1-e9f4-417f-adf4-f71accc470cb", "Makromapa Test01", "118364847911502210416", PREMIUM_USER_TOKEN,
      "b146b422-475c-4beb-9e9c-4e33e2288b08");

  public static ExternalMockUser APPLE_NEW_USER = new ExternalMockUser(null, null, "000807.4f911cd6e03a4ebb933c4e099a7ad710.2158", APPLE_USER_TOKEN, null);


  // -- Email&Password UserDetails
  public static PasswordMockUser NEW_REG_USER = new PasswordMockUser(null, "new_user@domain.com", "P@ssw0rd1", null);
  public static PasswordMockUser REG_USER = new PasswordMockUser("bb2cc695-4788-4470-8292-8e2d1870cd53", "user_1@example.com", "P@ssw0rd1", null);
  public static PasswordMockUser REG_USER_2 = new PasswordMockUser("228a4e7d-7023-49e4-afff-e7005c06225d", "user_2@example.com", "P@ssw0rd1", null);

  public static PasswordMockUser REG_DRAFT_USER = new PasswordMockUser("96718946-4ebd-4637-ae02-5cf2b5bc1bb2", "draft_user_1@email.pl", "P@ssw0rd1", "03c88cfd49");
  public static PasswordMockUser REG_DRAFT_USER_WITH_EXPIRED_CODE = new PasswordMockUser("876003b2-2454-47b1-8145-0037e7069178", "draft_user_2@email.pl", "P@ssw0rd1", "S0905WDgha");
  public static PasswordMockUser REG_DRAFT_USER_WITH_DISABLED_CODE = new PasswordMockUser("0385d294-acf8-474b-b265-102f76ef9ae2", "draft_user_3@email.pl", "P@ssw0rd1", "HHEH8x01B6");

  @Value
  @EqualsAndHashCode(callSuper = true)
  public static class ExternalMockUser extends BaseMockUser {

    String externalId;
    String jwt;
    Header authenticationHeader;
    String accessCode;

    public ExternalMockUser(String userId, String name, String externalId, String jwt, String accessCode) {
      super(userId, name);
      this.externalId = externalId;
      this.jwt = jwt;
      this.accessCode = accessCode;
      this.authenticationHeader = new Header(AUTHORIZATION, BEARER_TOKEN + jwt);
    }
  }

  @Value
  @EqualsAndHashCode(callSuper = true)
  public static class PasswordMockUser extends BaseMockUser {

    String password;
    String activationCode;

    public PasswordMockUser(String userId, String name, String password, String activationCode) {
      super(userId, name);
      this.password = password;
      this.activationCode = activationCode;
    }
  }

  @Value
  @NonFinal
  public static abstract class BaseMockUser {

    UUID userId;
    String name;

    public BaseMockUser(String userId, String name) {
      this.userId = userId == null ? null : fromString(userId);
      this.name = name;
    }
  }
}