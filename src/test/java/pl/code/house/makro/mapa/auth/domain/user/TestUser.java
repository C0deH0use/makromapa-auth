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
  public static final String NEW_USER_TOKEN = "eyJhbGciOiJSUzI1NiIsImtpZCI6IjEzZThkNDVhNDNjYjIyNDIxNTRjN2Y0ZGFmYWMyOTMzZmVhMjAzNzQiLCJ0eXAiOiJKV1QifQ.eyJpc3MiOiJodHRwczovL2FjY291bnRzLmdvb2dsZS5jb20iLCJhenAiOiI1NjQ4MTI2MDYxOTgtcHNqbzAwamJ1bWFzMTE3cGV1YjVoMzN2ODN0NGozaXAuYXBwcy5nb29nbGV1c2VyY29udGVudC5jb20iLCJhdWQiOiI1NjQ4MTI2MDYxOTgtcHNqbzAwamJ1bWFzMTE3cGV1YjVoMzN2ODN0NGozaXAuYXBwcy5nb29nbGV1c2VyY29udGVudC5jb20iLCJzdWIiOiIxMDQ3OTY4NjU4ODE0ODEwMzQzMDAiLCJlbWFpbCI6ImNvZGVob3VzZS5tYWtyb21hcGFAZ21haWwuY29tIiwiZW1haWxfdmVyaWZpZWQiOnRydWUsImF0X2hhc2giOiJ6clNIWTEtQTUyWml1QVhuY2pTak53Iiwibm9uY2UiOiJKdjViOWtwLVV3M0JfYzgzQzk5OVhOR2duLTBMMFItQk9sd2FDZ2VzWEM4IiwibmFtZSI6Ik1ha3JvIE1hcGEiLCJwaWN0dXJlIjoiaHR0cHM6Ly9saDMuZ29vZ2xldXNlcmNvbnRlbnQuY29tL2EtL0FPaDE0R2p4elJMaUZDT0dMZ0JqV2dqX2ZsM2QzZFdKNnE5Ny1vZXR2WXRoPXM5Ni1jIiwiZ2l2ZW5fbmFtZSI6Ik1ha3JvIiwiZmFtaWx5X25hbWUiOiJNYXBhIiwibG9jYWxlIjoicGwiLCJpYXQiOjE2MTczMDY4MDEsImV4cCI6MTYxNzMxMDQwMX0.UxljKCzJVXXJY46ISNDK4Ts20BYR3RFmMEtU7lI5yBHz1LREx5KmAic8FFd9SeoqvcHZUH6hzUHiEIjfOGMV2VQoG7yl-DbaG0yBd9TOVT7EHcQLS81euzKli53aLwFJ27KNQJUezbSeBBNGaVUs73sixAEAsID4JpjS_NOT5XUmjREPbtKlpbjRqZN-6AFLdo9LMyZTgZ_C0Dvhay1oEX3yOImham5fAY30Op_mzMSkXlSWVYQjM6dsL0LR3GrmoqhKR2BzOOMzilH18Qg47W6BCMRc5WX64q39aFMg_wycVES4bDy9Nd4aCmG-nwoSGet0QGQPVBPtm5IF7fBApg";

  //test.makro01@gmail.com
  public static final String PREMIUM_USER_TOKEN = "eyJhbGciOiJSUzI1NiIsImtpZCI6IjEzZThkNDVhNDNjYjIyNDIxNTRjN2Y0ZGFmYWMyOTMzZmVhMjAzNzQiLCJ0eXAiOiJKV1QifQ.eyJpc3MiOiJodHRwczovL2FjY291bnRzLmdvb2dsZS5jb20iLCJhenAiOiI1NjQ4MTI2MDYxOTgtcHNqbzAwamJ1bWFzMTE3cGV1YjVoMzN2ODN0NGozaXAuYXBwcy5nb29nbGV1c2VyY29udGVudC5jb20iLCJhdWQiOiI1NjQ4MTI2MDYxOTgtcHNqbzAwamJ1bWFzMTE3cGV1YjVoMzN2ODN0NGozaXAuYXBwcy5nb29nbGV1c2VyY29udGVudC5jb20iLCJzdWIiOiIxMTgzNjQ4NDc5MTE1MDIyMTA0MTYiLCJlbWFpbCI6InRlc3QubWFrcm8wMUBnbWFpbC5jb20iLCJlbWFpbF92ZXJpZmllZCI6dHJ1ZSwiYXRfaGFzaCI6IlJYcy1LbVF6QWllclRBZ1lLckJ3RHciLCJub25jZSI6Im1JbjNkekE2LU5iVGxKSTFnZEx4ZFZnc2NhMzZYb3dld0FPX1lLLUNTcWciLCJuYW1lIjoiTWFrcm9tYXBhIFRlc3QwMSIsInBpY3R1cmUiOiJodHRwczovL2xoMy5nb29nbGV1c2VyY29udGVudC5jb20vLWlrWV9DbTcyY3p3L0FBQUFBQUFBQUFJL0FBQUFBQUFBQUFBL0FNWnV1Y2xPWXMtbFFteEFXYWhLNE5ocVlVLVcycGN5N2cvczk2LWMvcGhvdG8uanBnIiwiZ2l2ZW5fbmFtZSI6Ik1ha3JvbWFwYSIsImZhbWlseV9uYW1lIjoiVGVzdDAxIiwibG9jYWxlIjoicGwiLCJpYXQiOjE2MTczMDY0OTEsImV4cCI6MTYxNzMxMDA5MX0.JAaSRy06qAdpqazPNbA6T-tYa9wfPc1VOvPalRawC4fMuuQhxY2KanuBegA4WR99Gkj0URiTqQzVVdaIIuTuTmrJWWZdf18J1l6usGTQy3pDrvLa98Z76a8nFlQgDHeD07SUZZyyvHfOWdZESDld-C8VnDQqunV-LD4YbLsebSBBrFKJ5VybrofHXA03mXEX2dK-r8kbVsoGekQIsoPN4Ry9XYjj3VJGXvXaU-L1g1aRzrhTSO1kbtzP_HC9tCgvMBrxF43Ps4Csp1Wddy2bpzzEJUb5r4p0SawMGv5f9uLKE8abNeo4Y0XbYY4ObR67TEv-N6xqtQuLWuSXXzryDg";

  //info@code-house.pl
  public static final String APPLE_USER_TOKEN = "eyJraWQiOiI4NkQ4OEtmIiwiYWxnIjoiUlMyNTYifQ.eyJpc3MiOiJodHRwczovL2FwcGxlaWQuYXBwbGUuY29tIiwiYXVkIjoicGwuY29kZWhvdXNlLm1ha3JvLm1hcGEubW9iaWxlIiwiZXhwIjoxNjAyNjIxODI3LCJpYXQiOjE2MDI1MzU0MjcsInN1YiI6IjAwMDk2Ny5iNWI5NjU2OGE2NGM0YmQyOGFlYzU5ZDYxODYzOTllMS4wODM3IiwiY19oYXNoIjoicHVzdlRiRE16aG9hVW9zNjdIRERMZyIsImVtYWlsIjoiaW5mb0Bjb2RlLWhvdXNlLnBsIiwiZW1haWxfdmVyaWZpZWQiOiJ0cnVlIiwiYXV0aF90aW1lIjoxNjAyNTM1NDI3LCJub25jZV9zdXBwb3J0ZWQiOnRydWUsInJlYWxfdXNlcl9zdGF0dXMiOjJ9.XicD9rNnYKOqbUmvIq0GkGZVS7saHUQcNajX7jvT2WXrrTMhKSRlGoS_RXgAO2WWaE4gJehqyBLOd4Xq_Gju9Y7dQEn6tccM6qZW_y59RTmv9iL_vI3n3WYzeHkDJcPzeotP2ADKTSMRBBSbd-GLUtg_5qdFOEsmZXED4rpY-m_1RlDW0aiisBtrS8jD5WQr3MSfvGy-9dCdnCtAAgwSuooW1KqxY5Iu3s2GZdFo7l1iyuVYlZCabo83f50ohsz-lnJc8uHVusJij-MlxGMEXXZ8gUjcpHwzuMLmCPoolS1_xdYT_1HSgfZU9mBgm6-TmX-MxQ7VrChUkAuAwUT79Q";

  //test.makro01@gmail.com
  public static final String FACEBOOK_USER_ACCESS_CODE = "EAAIyey5GNe0BAOgglL3L1soAQpXKUVrx2XFY0Ko0QnGzUzGbj1uOyBO63CguubkAhT77sZCupoUO3ZArZCPHLzbJA0c8HXiOOHyXvhJ2zdNbmH6yMDq0aibQTIdBsapvSHVyd2YZBGWxHfuBemR7cUxEdJ1ze5pEs0coGSQZA8rFhkgZA39vx4Fbl2CrQOhljDPKZCuSPQZC85MFyEtiiwNzgqsKS15wXupz82NZCZCWMNsGOsWMgmR8j3";

  // -- External(JWT) UserDetails
  public static ExternalMockUser GOOGLE_NEW_USER = new ExternalMockUser(null, "Makro Mapa", "104796865881481034300", NEW_USER_TOKEN, null);
  public static ExternalMockUser GOOGLE_PREMIUM_USER = new ExternalMockUser("aa6641c1-e9f4-417f-adf4-f71accc470cb", "Makromapa Test01", "118364847911502210416", PREMIUM_USER_TOKEN,
      "7e1be788-880d-451b-be37-15d6e03762fa");

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