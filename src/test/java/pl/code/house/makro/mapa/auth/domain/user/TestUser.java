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
  public static final String NEW_USER_TOKEN = "eyJhbGciOiJSUzI1NiIsImtpZCI6ImFkZDhjMGVlNjIzOTU0NGFmNTNmOTM3MTJhNTdiMmUyNmY5NDMzNTIiLCJ0eXAiOiJKV1QifQ.eyJpc3MiOiJodHRwczovL2FjY291bnRzLmdvb2dsZS5jb20iLCJhenAiOiI1NjQ4MTI2MDYxOTgtN2cxdnRoNHI2OGp1dHNuaDJkMnE4bDBpbWtxaW0wcXYuYXBwcy5nb29nbGV1c2VyY29udGVudC5jb20iLCJhdWQiOiI1NjQ4MTI2MDYxOTgtdTZiNXRvOHJoanNxa3BpOXNpZHNlazN2cmswMmtqcTUuYXBwcy5nb29nbGV1c2VyY29udGVudC5jb20iLCJzdWIiOiIxMDQ3OTY4NjU4ODE0ODEwMzQzMDAiLCJlbWFpbCI6ImNvZGVob3VzZS5tYWtyb21hcGFAZ21haWwuY29tIiwiZW1haWxfdmVyaWZpZWQiOnRydWUsIm5hbWUiOiJNYWtybyBNYXBhIiwicGljdHVyZSI6Imh0dHBzOi8vbGgzLmdvb2dsZXVzZXJjb250ZW50LmNvbS9hLS9BT2gxNEdqeHpSTGlGQ09HTGdCaldnal9mbDNkM2RXSjZxOTctb2V0dll0aD1zOTYtYyIsImdpdmVuX25hbWUiOiJNYWtybyIsImZhbWlseV9uYW1lIjoiTWFwYSIsImxvY2FsZSI6InBsIiwiaWF0IjoxNjM0MTYyNjI2LCJleHAiOjE2MzQxNjYyMjZ9.F9O4dgGrUFTti9rl3ejhKoThAWPO1qOXd77B_lqHgLMq8XR18oFqgKN4b6eXsjlyI_PORSD5aRXKlXOvUAI8RFP8-6FVdS4RDm67NJbVoY-oVYXUKsRPbzX8Tqn3xPvuc8ScBkvh-wRSI-s2MyT-miceWpa_9mDPFE7XCD0_fN8-HZSCiGaeM0-uL9bqpydS2fi5EXAcpJT3dnX2_d3SZQ5uXYrz6rdi8x83d8iMjhu9DjJhaJAnUnad3b9u8K2X4ESADHoeXCOtw8nBfmqRSxOgtKm2EdyxG4u3qpWrBmyKdxVZ7W7uRpkmfnUOwzdTNp6wk5PDnA5ZqrLgLBjLqg";

  //test.makro01@gmail.com
  public static final String PREMIUM_USER_TOKEN = "eyJhbGciOiJSUzI1NiIsImtpZCI6ImFkZDhjMGVlNjIzOTU0NGFmNTNmOTM3MTJhNTdiMmUyNmY5NDMzNTIiLCJ0eXAiOiJKV1QifQ.eyJpc3MiOiJodHRwczovL2FjY291bnRzLmdvb2dsZS5jb20iLCJhenAiOiI1NjQ4MTI2MDYxOTgtN2cxdnRoNHI2OGp1dHNuaDJkMnE4bDBpbWtxaW0wcXYuYXBwcy5nb29nbGV1c2VyY29udGVudC5jb20iLCJhdWQiOiI1NjQ4MTI2MDYxOTgtdTZiNXRvOHJoanNxa3BpOXNpZHNlazN2cmswMmtqcTUuYXBwcy5nb29nbGV1c2VyY29udGVudC5jb20iLCJzdWIiOiIxMTgzNjQ4NDc5MTE1MDIyMTA0MTYiLCJlbWFpbCI6InRlc3QubWFrcm8wMUBnbWFpbC5jb20iLCJlbWFpbF92ZXJpZmllZCI6dHJ1ZSwibmFtZSI6Ik1ha3JvbWFwYSBUZXN0MDEiLCJwaWN0dXJlIjoiaHR0cHM6Ly9saDMuZ29vZ2xldXNlcmNvbnRlbnQuY29tL2EvQUFUWEFKeUdLaHlBd0g2LWlESnNrVnl2YVBHMnppZVlpM2daakJUbjdJQT1zOTYtYyIsImdpdmVuX25hbWUiOiJNYWtyb21hcGEiLCJmYW1pbHlfbmFtZSI6IlRlc3QwMSIsImxvY2FsZSI6InBsIiwiaWF0IjoxNjM0MTYyNDI1LCJleHAiOjE2MzQxNjYwMjV9.T1g503Qp3zlYEaldAXu4s8fTRC7B-goaJNkMQaNQbBCXqkycHEAWFZ8E0VuYD3Jk7Rle6gyiJs_xEyE-d39x6xwuUZam1I0sOYKAo31DogOiZ3epT1BjVpGDwYDXmO-1B2oIKTSpuVv_ZLxcHuFkVwvvR51FnjWbW-fCjUu6XqAQxiYg6GoDb056g0eKFYwjBqw64XMIGPM3JhaxaK8eBsL6ar9nNxUigRcS4CQpSfVZzL6Tg4EEmsF9b6Y67IVr8MFlu0UIsPLkV1AfSUcqvP7dUEprZl4OrbkxkYDrOLs071Zn0wALYFPIH4i-AdAMWF35S50atXnGpRaORnmnPw";

  //info@code-house.pl
  public static final String APPLE_USER_TOKEN = "eyJraWQiOiI4NkQ4OEtmIiwiYWxnIjoiUlMyNTYifQ.eyJpc3MiOiJodHRwczovL2FwcGxlaWQuYXBwbGUuY29tIiwiYXVkIjoicGwuY29kZWhvdXNlLm1ha3JvLm1hcGEubW9iaWxlIiwiZXhwIjoxNjAyNjIxODI3LCJpYXQiOjE2MDI1MzU0MjcsInN1YiI6IjAwMDk2Ny5iNWI5NjU2OGE2NGM0YmQyOGFlYzU5ZDYxODYzOTllMS4wODM3IiwiY19oYXNoIjoicHVzdlRiRE16aG9hVW9zNjdIRERMZyIsImVtYWlsIjoiaW5mb0Bjb2RlLWhvdXNlLnBsIiwiZW1haWxfdmVyaWZpZWQiOiJ0cnVlIiwiYXV0aF90aW1lIjoxNjAyNTM1NDI3LCJub25jZV9zdXBwb3J0ZWQiOnRydWUsInJlYWxfdXNlcl9zdGF0dXMiOjJ9.XicD9rNnYKOqbUmvIq0GkGZVS7saHUQcNajX7jvT2WXrrTMhKSRlGoS_RXgAO2WWaE4gJehqyBLOd4Xq_Gju9Y7dQEn6tccM6qZW_y59RTmv9iL_vI3n3WYzeHkDJcPzeotP2ADKTSMRBBSbd-GLUtg_5qdFOEsmZXED4rpY-m_1RlDW0aiisBtrS8jD5WQr3MSfvGy-9dCdnCtAAgwSuooW1KqxY5Iu3s2GZdFo7l1iyuVYlZCabo83f50ohsz-lnJc8uHVusJij-MlxGMEXXZ8gUjcpHwzuMLmCPoolS1_xdYT_1HSgfZU9mBgm6-TmX-MxQ7VrChUkAuAwUT79Q";

  //test.makro01@gmail.com
  public static final String FACEBOOK_USER_ACCESS_CODE = "EAAIyey5GNe0BAPuqPoTwvIZBC4OZA9WDE9E4iifPYdUbg887Gw6NuOVQx6thIrDZCBuozRL53xJGNkp4EsJhaOgjIVQxikK9eBs7gkT3R1kG3oGmTVKSwxYdvZCayRj3dWNU7TAj6W4aLB4odLvGtZCy1f4whUdHORj2VJLPUx7gPIOzdtAfkrTkd6KWNM5FlZAljRHyhP8iJFqPusRk8zqXExzqHdYT0TMToZCOUiAkVIdJxWxtcFHqxiPyEHHnd4ZD";

  // -- External(JWT) UserDetails
  public static ExternalMockUser GOOGLE_NEW_USER = new ExternalMockUser(null, "Makro Mapa", "104796865881481034300", NEW_USER_TOKEN, null);
  public static ExternalMockUser GOOGLE_PREMIUM_USER = new ExternalMockUser("aa6641c1-e9f4-417f-adf4-f71accc470cb", "Makromapa Test01", "118364847911502210416", PREMIUM_USER_TOKEN,
      "vxHBCqPryWCNe2gM69XqKc2Xewc");

  public static ExternalMockUser APPLE_NEW_USER = new ExternalMockUser(null, null, "000967.b5b96568a64c4bd28aec59d6186399e1.0837", APPLE_USER_TOKEN, null);

  public static ExternalMockUser FACEBOOK_NEW_USER = new ExternalMockUser(null, null, "112263577290252", FACEBOOK_USER_ACCESS_CODE, null);


  // -- Email&Password UserDetails
  public static PasswordMockUser NEW_REG_USER = new PasswordMockUser(null, "new_user@domain.com", "P@ssw0rd1", null);
  public static PasswordMockUser REG_USER = new PasswordMockUser("bb2cc695-4788-4470-8292-8e2d1870cd53", "user_1@example.com", "P@ssw0rd1", null);
  public static PasswordMockUser REG_USER_2 = new PasswordMockUser("228a4e7d-7023-49e4-afff-e7005c06225d", "user_2@example.com", "P@ssw0rd1", null);
  public static PasswordMockUser ADMIN = new PasswordMockUser("401c7fa3-ff8f-4aa6-a43f-299e6a48b16c", "admin@example.com", "P@ssw0rd1", null);

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