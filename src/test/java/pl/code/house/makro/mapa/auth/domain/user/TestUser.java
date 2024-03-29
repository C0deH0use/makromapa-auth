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
  public static final String NEW_USER_TOKEN = "eyJhbGciOiJSUzI1NiIsImtpZCI6ImNlYzEzZGViZjRiOTY0Nzk2ODM3MzYyMDUwODI0NjZjMTQ3OTdiZDAiLCJ0eXAiOiJKV1QifQ.eyJpc3MiOiJodHRwczovL2FjY291bnRzLmdvb2dsZS5jb20iLCJhenAiOiI1NjQ4MTI2MDYxOTgtN2cxdnRoNHI2OGp1dHNuaDJkMnE4bDBpbWtxaW0wcXYuYXBwcy5nb29nbGV1c2VyY29udGVudC5jb20iLCJhdWQiOiI1NjQ4MTI2MDYxOTgtdTZiNXRvOHJoanNxa3BpOXNpZHNlazN2cmswMmtqcTUuYXBwcy5nb29nbGV1c2VyY29udGVudC5jb20iLCJzdWIiOiIxMDQ3OTY4NjU4ODE0ODEwMzQzMDAiLCJlbWFpbCI6ImNvZGVob3VzZS5tYWtyb21hcGFAZ21haWwuY29tIiwiZW1haWxfdmVyaWZpZWQiOnRydWUsIm5hbWUiOiJNYWtybyBNYXBhIiwicGljdHVyZSI6Imh0dHBzOi8vbGgzLmdvb2dsZXVzZXJjb250ZW50LmNvbS9hLS9BT2gxNEdqeHpSTGlGQ09HTGdCaldnal9mbDNkM2RXSjZxOTctb2V0dll0aD1zOTYtYyIsImdpdmVuX25hbWUiOiJNYWtybyIsImZhbWlseV9uYW1lIjoiTWFwYSIsImxvY2FsZSI6InBsIiwiaWF0IjoxNjQ4NTAwMzU1LCJleHAiOjE2NDg1MDM5NTV9.LSC1NTmoFS9HWOOlivA4w2LpcsiP-twy9P1Yqx7U4jLrvvEW86nnXTUrFCSYTs_qly6sqS-ksho9d6W7ebIXIdgtwdT81RlDRzDYmUJWOyK-yUUhwrnasaRvo-4A6nY9kVriaD-vwakBLfB1w3vJunrosr9KFv_B5grniWkLlrIlJ6XDl8CEOhHPC20rga9_DxoqI9Az1hLpj8TmdfOPJkLuZZ8J2KLhcNx8j8HTHcSPsnAaGjqcPhVp006kE_CsiSZ0mmFgQekBmUBh1JuejAlG6aNQEHh0bJB_javtNmUv7GTFiw-5Gpu6UAUhfs13iv9ojAbkm199neOrct8jXg";

  //test.makro01@gmail.com
  public static final String PREMIUM_USER_TOKEN = "eyJhbGciOiJSUzI1NiIsImtpZCI6ImNlYzEzZGViZjRiOTY0Nzk2ODM3MzYyMDUwODI0NjZjMTQ3OTdiZDAiLCJ0eXAiOiJKV1QifQ.eyJpc3MiOiJodHRwczovL2FjY291bnRzLmdvb2dsZS5jb20iLCJhenAiOiI1NjQ4MTI2MDYxOTgtN2cxdnRoNHI2OGp1dHNuaDJkMnE4bDBpbWtxaW0wcXYuYXBwcy5nb29nbGV1c2VyY29udGVudC5jb20iLCJhdWQiOiI1NjQ4MTI2MDYxOTgtdTZiNXRvOHJoanNxa3BpOXNpZHNlazN2cmswMmtqcTUuYXBwcy5nb29nbGV1c2VyY29udGVudC5jb20iLCJzdWIiOiIxMTgzNjQ4NDc5MTE1MDIyMTA0MTYiLCJlbWFpbCI6InRlc3QubWFrcm8wMUBnbWFpbC5jb20iLCJlbWFpbF92ZXJpZmllZCI6dHJ1ZSwibmFtZSI6Ik1ha3JvbWFwYSBUZXN0MDEiLCJwaWN0dXJlIjoiaHR0cHM6Ly9saDMuZ29vZ2xldXNlcmNvbnRlbnQuY29tL2EvQUFUWEFKeUdLaHlBd0g2LWlESnNrVnl2YVBHMnppZVlpM2daakJUbjdJQT1zOTYtYyIsImdpdmVuX25hbWUiOiJNYWtyb21hcGEiLCJmYW1pbHlfbmFtZSI6IlRlc3QwMSIsImxvY2FsZSI6InBsIiwiaWF0IjoxNjQ4NTAwMDYzLCJleHAiOjE2NDg1MDM2NjN9.S28Yhy7qnLFHnRwc9_3WqZuEiLFjjEcXJLFSCQagz-ddAz9gceWIHVSB4sKeB0jAjWCjq7bHPRbbzRsxK5fd3h8--55K1exQhVIfEXaiD84yKXiqEhPDvPldd4GYVdzx6CWjkO0_0w5_McXBXZx1gWZ_kkEQ_l9HUtTc9tBVcBaW3f5oNS9La-3bw7FLApOiwQs1wcxgY3GHqHWdzIoKjLZ9d2qWjD5Zx9xSwm5znvO-DiJ61iVhWBTzwj3O6XY_385dRYR1Jxuc0JMS5vOaPe4jNRlaUz6YrT4X4DYwNWu-gDTtEHmG1tWgYyTKjRMbg8GqE5iJPvTBSAlKiSir0Q";

  //info@code-house.pl
  public static final String APPLE_USER_TOKEN = "eyJraWQiOiI4NkQ4OEtmIiwiYWxnIjoiUlMyNTYifQ.eyJpc3MiOiJodHRwczovL2FwcGxlaWQuYXBwbGUuY29tIiwiYXVkIjoicGwuY29kZWhvdXNlLm1ha3JvLm1hcGEubW9iaWxlIiwiZXhwIjoxNjAyNjIxODI3LCJpYXQiOjE2MDI1MzU0MjcsInN1YiI6IjAwMDk2Ny5iNWI5NjU2OGE2NGM0YmQyOGFlYzU5ZDYxODYzOTllMS4wODM3IiwiY19oYXNoIjoicHVzdlRiRE16aG9hVW9zNjdIRERMZyIsImVtYWlsIjoiaW5mb0Bjb2RlLWhvdXNlLnBsIiwiZW1haWxfdmVyaWZpZWQiOiJ0cnVlIiwiYXV0aF90aW1lIjoxNjAyNTM1NDI3LCJub25jZV9zdXBwb3J0ZWQiOnRydWUsInJlYWxfdXNlcl9zdGF0dXMiOjJ9.XicD9rNnYKOqbUmvIq0GkGZVS7saHUQcNajX7jvT2WXrrTMhKSRlGoS_RXgAO2WWaE4gJehqyBLOd4Xq_Gju9Y7dQEn6tccM6qZW_y59RTmv9iL_vI3n3WYzeHkDJcPzeotP2ADKTSMRBBSbd-GLUtg_5qdFOEsmZXED4rpY-m_1RlDW0aiisBtrS8jD5WQr3MSfvGy-9dCdnCtAAgwSuooW1KqxY5Iu3s2GZdFo7l1iyuVYlZCabo83f50ohsz-lnJc8uHVusJij-MlxGMEXXZ8gUjcpHwzuMLmCPoolS1_xdYT_1HSgfZU9mBgm6-TmX-MxQ7VrChUkAuAwUT79Q";

  //test.makro01@gmail.com
  public static final String FACEBOOK_USER_ACCESS_CODE = "EAAIyey5GNe0BAC2gGZCpJjruZBOC09sPAoMROTZCZBHHTTgO8CanH8rtUh5R9NpGvwWqGSIOje9KffARhaiqTcc692k7f1l2Y2C5FJgIqUH4n0gnih0OCk1RebdxXs0VnwekwxU8qfwPVfXZCF3iZBLZCq5CXbPaZC1qp9FQ7jc8gPtDdLe1q7HPTEZA7CoRc4JPBrWOi7ej7ZAB5dEALWt64TnifherEp5ZC8cnsuiIeaEZBTBnm32OiPns";

  // -- External(JWT) UserDetails
  public static ExternalMockUser GOOGLE_NEW_USER = new ExternalMockUser(null, "Makro Mapa", "104796865881481034300", NEW_USER_TOKEN, null);
  public static ExternalMockUser GOOGLE_PREMIUM_USER = new ExternalMockUser("aa6641c1-e9f4-417f-adf4-f71accc470cb", "Makromapa Test01", "118364847911502210416", PREMIUM_USER_TOKEN,
      "vxHBCqPryWCNe2gM69XqKc2Xewc");

  public static ExternalMockUser APPLE_NEW_USER = new ExternalMockUser(null, null, "000967.b5b96568a64c4bd28aec59d6186399e1.0837", APPLE_USER_TOKEN, null);

  public static ExternalMockUser FACEBOOK_NEW_USER = new ExternalMockUser(null, null, "103132744850547", FACEBOOK_USER_ACCESS_CODE, null);


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