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
  public static final String NEW_USER_TOKEN = "eyJhbGciOiJSUzI1NiIsImtpZCI6IjBhN2RjMTI2NjQ1OTBjOTU3ZmZhZWJmN2I2NzE4Mjk3Yjg2NGJhOTEiLCJ0eXAiOiJKV1QifQ.eyJpc3MiOiJodHRwczovL2FjY291bnRzLmdvb2dsZS5jb20iLCJhenAiOiI1NjQ4MTI2MDYxOTgtcHNqbzAwamJ1bWFzMTE3cGV1YjVoMzN2ODN0NGozaXAuYXBwcy5nb29nbGV1c2VyY29udGVudC5jb20iLCJhdWQiOiI1NjQ4MTI2MDYxOTgtcHNqbzAwamJ1bWFzMTE3cGV1YjVoMzN2ODN0NGozaXAuYXBwcy5nb29nbGV1c2VyY29udGVudC5jb20iLCJzdWIiOiIxMDQ3OTY4NjU4ODE0ODEwMzQzMDAiLCJlbWFpbCI6ImNvZGVob3VzZS5tYWtyb21hcGFAZ21haWwuY29tIiwiZW1haWxfdmVyaWZpZWQiOnRydWUsImF0X2hhc2giOiJtQ2w1OXFnaDZLWXhnUVlZeFRRajVRIiwibm9uY2UiOiJrMWtOZ0hYMXJoNmUtUkNDMGVEcHlXekFTejVNbHhmWVZpNUw3WEVxd2YwIiwibmFtZSI6Ik1ha3JvIE1hcGEiLCJwaWN0dXJlIjoiaHR0cHM6Ly9saDMuZ29vZ2xldXNlcmNvbnRlbnQuY29tL2EtL0FPaDE0R2p4elJMaUZDT0dMZ0JqV2dqX2ZsM2QzZFdKNnE5Ny1vZXR2WXRoPXM5Ni1jIiwiZ2l2ZW5fbmFtZSI6Ik1ha3JvIiwiZmFtaWx5X25hbWUiOiJNYXBhIiwibG9jYWxlIjoicGwiLCJpYXQiOjE1OTg2NDExNjYsImV4cCI6MTU5ODY0NDc2Nn0.QD0KOprRBxmjMlhGSdF5u0qKYSNLWXuKnh_uO09MHKSrT2YcBSmAXdhj8BNcE8bKLi7modFmTQ5EKFufvUZnD0xMEJIAQW8HPbJeBdGNaBnZmoNyhgDfaoJD3Gi6MONyWvEdMLDkQTs2BxdXwC-McRsPN-Dbjrr5sbhPF-JBrsjf_CzCskhHanHQjYcRt3VKTU6K-6unsPbPIFoyOx_DGamzrWMR_nfM7eqoVpvdLjIlV9awIelhAzGT3qK9-92797Up1T7CMuLBgpT3QgX8wwMVpJAq1gAsquaPm-qLk7Tp9DWoiczXnwt8xLpNHBOOwdfgu-mthLwZa9j8IJ375Q";

  //test.makro01@gmail.com
  public static final String PREMIUM_USER_TOKEN = "eyJhbGciOiJSUzI1NiIsImtpZCI6IjBhN2RjMTI2NjQ1OTBjOTU3ZmZhZWJmN2I2NzE4Mjk3Yjg2NGJhOTEiLCJ0eXAiOiJKV1QifQ.eyJpc3MiOiJodHRwczovL2FjY291bnRzLmdvb2dsZS5jb20iLCJhenAiOiI1NjQ4MTI2MDYxOTgtcHNqbzAwamJ1bWFzMTE3cGV1YjVoMzN2ODN0NGozaXAuYXBwcy5nb29nbGV1c2VyY29udGVudC5jb20iLCJhdWQiOiI1NjQ4MTI2MDYxOTgtcHNqbzAwamJ1bWFzMTE3cGV1YjVoMzN2ODN0NGozaXAuYXBwcy5nb29nbGV1c2VyY29udGVudC5jb20iLCJzdWIiOiIxMTgzNjQ4NDc5MTE1MDIyMTA0MTYiLCJlbWFpbCI6InRlc3QubWFrcm8wMUBnbWFpbC5jb20iLCJlbWFpbF92ZXJpZmllZCI6dHJ1ZSwiYXRfaGFzaCI6IjZtaTA3VGNvWmVnRi1weUR4SEdxWHciLCJub25jZSI6ImhXbEJqT3FsQ3YyYm1QN0FtRHctRVAwSFVUR0pJdkdtNHVTRWJWazJnVjgiLCJuYW1lIjoiTWFrcm9tYXBhIFRlc3QwMSIsInBpY3R1cmUiOiJodHRwczovL2xoMy5nb29nbGV1c2VyY29udGVudC5jb20vLWlrWV9DbTcyY3p3L0FBQUFBQUFBQUFJL0FBQUFBQUFBQUFBL0FNWnV1Y2xPWXMtbFFteEFXYWhLNE5ocVlVLVcycGN5N2cvczk2LWMvcGhvdG8uanBnIiwiZ2l2ZW5fbmFtZSI6Ik1ha3JvbWFwYSIsImZhbWlseV9uYW1lIjoiVGVzdDAxIiwibG9jYWxlIjoicGwiLCJpYXQiOjE1OTg2NDEzMTAsImV4cCI6MTU5ODY0NDkxMH0.RecdB7bspZCANGKE6O5Q51PBBrTZMbe0skOZqxQ9RcFynpSvU1s0nWRyP55yu58G7IGsrV4KRuxi5m_Ptk9pFUnW9iaEnOR759YIryEWrswFWAF4bPJqzuMyAH15JIDN3urOxENEAUuKcw71q_a6dOU8tgK86FdePRFkF1cnHVToEnVfF6H1WN4Ta2Vk14Ic4dZiypRT1Jc4guJb6Ry9iLFN0V2VtzlD9TQa3jlz8oUkC7NM9YSILjjp10SzNvIfCxLhNkwcBnEp5vx3ITfi3NxiOzCZ4QB12lPRyDI3tblqnzxpBdKp9_5Ss36MLpAG1ABEO5T1ACtbUOglhW17mw";

  //info@code-house.pl
  public static final String APPLE_USER_TOKEN = "eyJraWQiOiJlWGF1bm1MIiwiYWxnIjoiUlMyNTYifQ.eyJpc3MiOiJodHRwczovL2FwcGxlaWQuYXBwbGUuY29tIiwiYXVkIjoicGwuY29kZWhvdXNlLm1ha3JvLm1hcGEubW9iaWxlIiwiZXhwIjoxNTk3Njk1NzA4LCJpYXQiOjE1OTc2OTUxMDgsInN1YiI6IjAwMDk2Ny5iNWI5NjU2OGE2NGM0YmQyOGFlYzU5ZDYxODYzOTllMS4wODM3IiwiY19oYXNoIjoiV1hTMUFEUFREd0lxODd2NE9PeWw2dyIsImF1dGhfdGltZSI6MTU5NzY5NTEwOCwibm9uY2Vfc3VwcG9ydGVkIjp0cnVlfQ.pptJ_oeF-AJi8f0EWbhqJCZpqvRrGjzNGH73tvcmobiJ7suEVPLBjHwFNKechx56SRQr6QgS9qoQqKw-QXgcbgZaPFvqrcyKcAb6DGhLeGAvIUh-L8a_IFyoHxdwQU0iglVXb9LSP4vzpNNUP2BWPDNucQ_Gk_kqatEUy0mRTVHfeT5gzgk1TOgsCMQDLz3cSFflktKk5jxeCFXjy-Tc2Qqk0IkwNQjfYg262HNAlrzTKFoLCr47xMvok9FCYMIJf4PsC7hpKUwFSbUx0LiG68_taRXH0dq0ODWjnxmpjk8gjQOJUKbkbQiiwNO9V843CEvJaAQfv8vsCcO89MPwiA";

  //test.makro01@gmail.com
  public static final String FACEBOOK_USER_ACCESS_CODE = "EAAIyey5GNe0BAMWEtmKSysljdZBnUMobjZB9kn5njYKZCE0iXynUzJRhqyCJT7mgxWoPHdlYjTaX5vGZCgviWBPzobqh7T6h5ZATYerqq119oh5gklHt5slLBf4vYRkYPdZBew8CIBhVg15PMOZBcY2rjZA366oWYbrHhQRsfUrkeO8KareNKCEJpfFgpxQ7J9Ex2YfxheQM1aexZAZBJC8ZBXXStYNUTXgzi0ZD";

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