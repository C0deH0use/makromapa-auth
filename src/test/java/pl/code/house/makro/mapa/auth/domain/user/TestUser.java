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
  public static final String NEW_USER_TOKEN = "eyJhbGciOiJSUzI1NiIsImtpZCI6IjVlZmZhNzZlZjMzZWNiNWUzNDZiZDUxMmQ3ZDg5YjMwZTQ3ZDhlOTgiLCJ0eXAiOiJKV1QifQ.eyJpc3MiOiJodHRwczovL2FjY291bnRzLmdvb2dsZS5jb20iLCJhenAiOiI1NjQ4MTI2MDYxOTgtcHNqbzAwamJ1bWFzMTE3cGV1YjVoMzN2ODN0NGozaXAuYXBwcy5nb29nbGV1c2VyY29udGVudC5jb20iLCJhdWQiOiI1NjQ4MTI2MDYxOTgtcHNqbzAwamJ1bWFzMTE3cGV1YjVoMzN2ODN0NGozaXAuYXBwcy5nb29nbGV1c2VyY29udGVudC5jb20iLCJzdWIiOiIxMDQ3OTY4NjU4ODE0ODEwMzQzMDAiLCJlbWFpbCI6ImNvZGVob3VzZS5tYWtyb21hcGFAZ21haWwuY29tIiwiZW1haWxfdmVyaWZpZWQiOnRydWUsImF0X2hhc2giOiJDTGh5NUkxMkRJSWZsX2dEYkVndGhRIiwibm9uY2UiOiJKY1RkanFET2VBV0FaNy1zQkd3Q2JNTC1tTUxBZG1VTVFmWko4WHVnM1dNIiwibmFtZSI6Ik1ha3JvIE1hcGEiLCJwaWN0dXJlIjoiaHR0cHM6Ly9saDMuZ29vZ2xldXNlcmNvbnRlbnQuY29tL2EtL0FPaDE0R2p4elJMaUZDT0dMZ0JqV2dqX2ZsM2QzZFdKNnE5Ny1vZXR2WXRoPXM5Ni1jIiwiZ2l2ZW5fbmFtZSI6Ik1ha3JvIiwiZmFtaWx5X25hbWUiOiJNYXBhIiwibG9jYWxlIjoicGwiLCJpYXQiOjE2MDIxODIzMzMsImV4cCI6MTYwMjE4NTkzM30.pm8UaSmp0QhXUGM7lzFhb6cRsqwoWBN5BdpyCLsn7Qhh8Q5nQl2mLvYNY-3pQO7Vy3qMopHNGH8hbmuQ3PW0vVe4Gu4AGnRyEJ3Tjge1i1ooMOAp3i5p9aGad-0Zg09bSsZF691qAmbiDGeqAHlDDWbe9-Yiq6ryeUoOALg5ingjtQh2338nMnpRpW4RyJvo_8ZGSwdN0FBY9Ow2U3NcLcwmthlXnW9pZ1j1H7TEc3ICgKRY9DKGAIzGIYb0mzcmtG3B8E9IWmfbLZx6yAKFlaY3uvY-V__Q0RNErk-GgE__4wCBx2L88R8A1Ce8DrrkDP4kPgSXIp46J8g7ef3ejA";

  //test.makro01@gmail.com
  public static final String PREMIUM_USER_TOKEN = "eyJhbGciOiJSUzI1NiIsImtpZCI6IjVlZmZhNzZlZjMzZWNiNWUzNDZiZDUxMmQ3ZDg5YjMwZTQ3ZDhlOTgiLCJ0eXAiOiJKV1QifQ.eyJpc3MiOiJodHRwczovL2FjY291bnRzLmdvb2dsZS5jb20iLCJhenAiOiI1NjQ4MTI2MDYxOTgtcHNqbzAwamJ1bWFzMTE3cGV1YjVoMzN2ODN0NGozaXAuYXBwcy5nb29nbGV1c2VyY29udGVudC5jb20iLCJhdWQiOiI1NjQ4MTI2MDYxOTgtcHNqbzAwamJ1bWFzMTE3cGV1YjVoMzN2ODN0NGozaXAuYXBwcy5nb29nbGV1c2VyY29udGVudC5jb20iLCJzdWIiOiIxMTgzNjQ4NDc5MTE1MDIyMTA0MTYiLCJlbWFpbCI6InRlc3QubWFrcm8wMUBnbWFpbC5jb20iLCJlbWFpbF92ZXJpZmllZCI6dHJ1ZSwiYXRfaGFzaCI6IlZfdDd1TE5EN2NQTGNCcW9vNnpWdUEiLCJub25jZSI6ImdBd0xxN29JTVRiQmNmX1VrNWxGa29jVVdKZ0hHdExucDlsUUs0LWM2Q00iLCJuYW1lIjoiTWFrcm9tYXBhIFRlc3QwMSIsInBpY3R1cmUiOiJodHRwczovL2xoMy5nb29nbGV1c2VyY29udGVudC5jb20vLWlrWV9DbTcyY3p3L0FBQUFBQUFBQUFJL0FBQUFBQUFBQUFBL0FNWnV1Y2xPWXMtbFFteEFXYWhLNE5ocVlVLVcycGN5N2cvczk2LWMvcGhvdG8uanBnIiwiZ2l2ZW5fbmFtZSI6Ik1ha3JvbWFwYSIsImZhbWlseV9uYW1lIjoiVGVzdDAxIiwibG9jYWxlIjoicGwiLCJpYXQiOjE2MDIxODQ2NjIsImV4cCI6MTYwMjE4ODI2Mn0.E4yj-YjJH_neSxv-xD3_PQOyfl6190LeVfjH84xBpdN9FBhnsw2ikXjzro3vhzoZP_S7x3ojdVoXhJdROaYnSSk0CpUkNBbxnO1GVKRb0DukrZID5vAmwL5R3teZJlp0JeG1gcT98S-nsTCXhL0MPMEbfFpSgTjKWh6BCZiNYEzqOlJuegsnP_2_Yiuia8fFkw6Aip2ZTzIZdHFqxIfgd5-9SQQRK0ETNbagm3YkvfprdgsfOUI6W6dDG8Gra8vFrLZBhRO4ZsvMw1d_aV1XF2w-5qla_6HFWQ8XUBIBhABc2FmS1Ogu49gnRF8AU1KrkaFZcyYYeVVBdfQjBF3Zjg";

  //info@code-house.pl
  public static final String APPLE_USER_TOKEN = "eyJraWQiOiI4NkQ4OEtmIiwiYWxnIjoiUlMyNTYifQ.eyJpc3MiOiJodHRwczovL2FwcGxlaWQuYXBwbGUuY29tIiwiYXVkIjoicGwuY29kZWhvdXNlLm1ha3JvLm1hcGEubW9iaWxlIiwiZXhwIjoxNjAyNjIxODI3LCJpYXQiOjE2MDI1MzU0MjcsInN1YiI6IjAwMDk2Ny5iNWI5NjU2OGE2NGM0YmQyOGFlYzU5ZDYxODYzOTllMS4wODM3IiwiY19oYXNoIjoicHVzdlRiRE16aG9hVW9zNjdIRERMZyIsImVtYWlsIjoiaW5mb0Bjb2RlLWhvdXNlLnBsIiwiZW1haWxfdmVyaWZpZWQiOiJ0cnVlIiwiYXV0aF90aW1lIjoxNjAyNTM1NDI3LCJub25jZV9zdXBwb3J0ZWQiOnRydWUsInJlYWxfdXNlcl9zdGF0dXMiOjJ9.XicD9rNnYKOqbUmvIq0GkGZVS7saHUQcNajX7jvT2WXrrTMhKSRlGoS_RXgAO2WWaE4gJehqyBLOd4Xq_Gju9Y7dQEn6tccM6qZW_y59RTmv9iL_vI3n3WYzeHkDJcPzeotP2ADKTSMRBBSbd-GLUtg_5qdFOEsmZXED4rpY-m_1RlDW0aiisBtrS8jD5WQr3MSfvGy-9dCdnCtAAgwSuooW1KqxY5Iu3s2GZdFo7l1iyuVYlZCabo83f50ohsz-lnJc8uHVusJij-MlxGMEXXZ8gUjcpHwzuMLmCPoolS1_xdYT_1HSgfZU9mBgm6-TmX-MxQ7VrChUkAuAwUT79Q";

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