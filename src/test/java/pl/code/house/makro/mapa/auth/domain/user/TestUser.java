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
  public static final String NEW_USER_TOKEN = "eyJhbGciOiJSUzI1NiIsImtpZCI6IjJjNmZhNmY1OTUwYTdjZTQ2NWZjZjI0N2FhMGIwOTQ4MjhhYzk1MmMiLCJ0eXAiOiJKV1QifQ.eyJpc3MiOiJodHRwczovL2FjY291bnRzLmdvb2dsZS5jb20iLCJhenAiOiI1NjQ4MTI2MDYxOTgtcHNqbzAwamJ1bWFzMTE3cGV1YjVoMzN2ODN0NGozaXAuYXBwcy5nb29nbGV1c2VyY29udGVudC5jb20iLCJhdWQiOiI1NjQ4MTI2MDYxOTgtcHNqbzAwamJ1bWFzMTE3cGV1YjVoMzN2ODN0NGozaXAuYXBwcy5nb29nbGV1c2VyY29udGVudC5jb20iLCJzdWIiOiIxMDQ3OTY4NjU4ODE0ODEwMzQzMDAiLCJlbWFpbCI6ImNvZGVob3VzZS5tYWtyb21hcGFAZ21haWwuY29tIiwiZW1haWxfdmVyaWZpZWQiOnRydWUsImF0X2hhc2giOiI4cFBLWXpXVy00Sk5xTDFsdVZIUjFBIiwibm9uY2UiOiI2emJlSWhwZS1ZRXNKbktWOG5BeDBRdUx6U0xoc3A5dmg5UXZxbzZGcG9RIiwibmFtZSI6Ik1ha3JvIE1hcGEiLCJwaWN0dXJlIjoiaHR0cHM6Ly9saDMuZ29vZ2xldXNlcmNvbnRlbnQuY29tL2EtL0FPaDE0R2p4elJMaUZDT0dMZ0JqV2dqX2ZsM2QzZFdKNnE5Ny1vZXR2WXRoPXM5Ni1jIiwiZ2l2ZW5fbmFtZSI6Ik1ha3JvIiwiZmFtaWx5X25hbWUiOiJNYXBhIiwibG9jYWxlIjoicGwiLCJpYXQiOjE2MDE0MTY5MDcsImV4cCI6MTYwMTQyMDUwN30.kAtJvKw0JRlLyLAYC536Hu8Vio1VFzdtOoW6J2_7ZiZQVfVIGGTuTiotth9kkKgowvoZ1mmtSXNSKW6_xnpN-Cum2wJt7gtIcDlmkDCeoL8vv0tTqsw9-rIdgWQvVJzD9jte-rx_QhflKQCMl5eoZV1T_gX0fODOeHUtDttsg8Bl4JR90ux6USZUGIO6JmJD5d1urVWu15ZVgKzSzKkySxsZCVuYke69agcv859xP32n_sKrHa1-WQxzmkEBqeU_MPWlTXE1v5rPlpR1D_Kt4gtPmJxgFs1jz517ZJt-8j_yFBnl1VZw2yQYfhIW7cinOq5aopCJP4mOESh15AaFuQ";

  //test.makro01@gmail.com
  public static final String PREMIUM_USER_TOKEN = "eyJhbGciOiJSUzI1NiIsImtpZCI6IjJjNmZhNmY1OTUwYTdjZTQ2NWZjZjI0N2FhMGIwOTQ4MjhhYzk1MmMiLCJ0eXAiOiJKV1QifQ.eyJpc3MiOiJodHRwczovL2FjY291bnRzLmdvb2dsZS5jb20iLCJhenAiOiI1NjQ4MTI2MDYxOTgtcHNqbzAwamJ1bWFzMTE3cGV1YjVoMzN2ODN0NGozaXAuYXBwcy5nb29nbGV1c2VyY29udGVudC5jb20iLCJhdWQiOiI1NjQ4MTI2MDYxOTgtcHNqbzAwamJ1bWFzMTE3cGV1YjVoMzN2ODN0NGozaXAuYXBwcy5nb29nbGV1c2VyY29udGVudC5jb20iLCJzdWIiOiIxMTgzNjQ4NDc5MTE1MDIyMTA0MTYiLCJlbWFpbCI6InRlc3QubWFrcm8wMUBnbWFpbC5jb20iLCJlbWFpbF92ZXJpZmllZCI6dHJ1ZSwiYXRfaGFzaCI6ImUxUDFDNkdBZDNkd21qTDlXUnVjemciLCJub25jZSI6InBFc2ZmbnlIcHZUOGpjVURnSzhCLVB1b3h6Y192QmFMRnVvamlfa3NuYW8iLCJuYW1lIjoiTWFrcm9tYXBhIFRlc3QwMSIsInBpY3R1cmUiOiJodHRwczovL2xoMy5nb29nbGV1c2VyY29udGVudC5jb20vLWlrWV9DbTcyY3p3L0FBQUFBQUFBQUFJL0FBQUFBQUFBQUFBL0FNWnV1Y2xPWXMtbFFteEFXYWhLNE5ocVlVLVcycGN5N2cvczk2LWMvcGhvdG8uanBnIiwiZ2l2ZW5fbmFtZSI6Ik1ha3JvbWFwYSIsImZhbWlseV9uYW1lIjoiVGVzdDAxIiwibG9jYWxlIjoicGwiLCJpYXQiOjE2MDE0MTcwNTcsImV4cCI6MTYwMTQyMDY1N30.RZ1l69uonfjve5ZVL2f3IqiQEfB3yBKq5DVcOSbRTdExhLfD09HvSgyR9FrkapdbDRbNBHv9shon1T_J50P9T77bN3qliq9FE8UveJILaNnJkQ5Hgeb15udNQwu6n2I1qqPsAc2RaCnSWZpbr9FlHkIiArMtp97easXlMr6J3oDGsF1S0a3xdDFieQ_nybrp8uMK8JID3DuQcp6-4TsMeC6UHK7dWa_Gjb8lJxX0tFcXbdHHHX_67mwfpXv5UrSt2zKsTykcpuClhQbQedCiwRlaqX32m_8f1JRiCOQA5PfRKWLVZANwwwoP9ObiWZshfl4ibflqJuiK1zS_uXOpZQ";

  //info@code-house.pl
  public static final String APPLE_USER_TOKEN = "eyJraWQiOiJlWGF1bm1MIiwiYWxnIjoiUlMyNTYifQ.eyJpc3MiOiJodHRwczovL2FwcGxlaWQuYXBwbGUuY29tIiwiYXVkIjoicGwuY29kZWhvdXNlLm1ha3JvLm1hcGEubW9iaWxlIiwiZXhwIjoxNTk3Njk1NzA4LCJpYXQiOjE1OTc2OTUxMDgsInN1YiI6IjAwMDk2Ny5iNWI5NjU2OGE2NGM0YmQyOGFlYzU5ZDYxODYzOTllMS4wODM3IiwiY19oYXNoIjoiV1hTMUFEUFREd0lxODd2NE9PeWw2dyIsImF1dGhfdGltZSI6MTU5NzY5NTEwOCwibm9uY2Vfc3VwcG9ydGVkIjp0cnVlfQ.pptJ_oeF-AJi8f0EWbhqJCZpqvRrGjzNGH73tvcmobiJ7suEVPLBjHwFNKechx56SRQr6QgS9qoQqKw-QXgcbgZaPFvqrcyKcAb6DGhLeGAvIUh-L8a_IFyoHxdwQU0iglVXb9LSP4vzpNNUP2BWPDNucQ_Gk_kqatEUy0mRTVHfeT5gzgk1TOgsCMQDLz3cSFflktKk5jxeCFXjy-Tc2Qqk0IkwNQjfYg262HNAlrzTKFoLCr47xMvok9FCYMIJf4PsC7hpKUwFSbUx0LiG68_taRXH0dq0ODWjnxmpjk8gjQOJUKbkbQiiwNO9V843CEvJaAQfv8vsCcO89MPwiA";

  //test.makro01@gmail.com
  public static final String FACEBOOK_USER_ACCESS_CODE = "EAAIyey5GNe0BAJJQ7WpNEvZCfk3xuJtZAnMTt7j32aNJHd6k5inGa5A6SE7rGcuXbaboFB7pPeZCi1ZCZCKBtRTtL6l7ymYpP2fpXVsnTfaGncQ7Lrr6U0MWZAwtMz2tJdabBWkPrvogWImzyVMGZB5FZAgYh1eO2Vbd7DAcviIc3zA40gx6H82gZCO5OdR8saUfJ93htApVGGTizwhasuNCYIZAMNVtsC3hRH9Bc7zU5Lv9dasyk5uCL7";

  // -- External(JWT) UserDetails
  public static ExternalMockUser GOOGLE_NEW_USER = new ExternalMockUser(null, "Makro Mapa", "104796865881481034300", NEW_USER_TOKEN, null);
  public static ExternalMockUser GOOGLE_PREMIUM_USER = new ExternalMockUser("aa6641c1-e9f4-417f-adf4-f71accc470cb", "Makromapa Test01", "118364847911502210416", PREMIUM_USER_TOKEN,
      "b146b422-475c-4beb-9e9c-4e33e2288b08");

  public static ExternalMockUser APPLE_NEW_USER = new ExternalMockUser(null, null, "000967.b5b96568a64c4bd28aec59d6186399e1.0837", APPLE_USER_TOKEN, null);

  public static ExternalMockUser FACEBOOK_NEW_USER = new ExternalMockUser(null, null, "103132744850547", FACEBOOK_USER_ACCESS_CODE, null);


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