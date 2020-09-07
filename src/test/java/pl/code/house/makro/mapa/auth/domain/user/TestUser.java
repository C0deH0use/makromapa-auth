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
  public static final String NEW_USER_TOKEN = "eyJhbGciOiJSUzI1NiIsImtpZCI6IjNmMzMyYjNlOWI5MjhiZmU1MWJjZjRmOGRhNTQzY2M0YmQ5ZDQ3MjQiLCJ0eXAiOiJKV1QifQ.eyJpc3MiOiJodHRwczovL2FjY291bnRzLmdvb2dsZS5jb20iLCJhenAiOiI1NjQ4MTI2MDYxOTgtN2cxdnRoNHI2OGp1dHNuaDJkMnE4bDBpbWtxaW0wcXYuYXBwcy5nb29nbGV1c2VyY29udGVudC5jb20iLCJhdWQiOiI1NjQ4MTI2MDYxOTgtdTZiNXRvOHJoanNxa3BpOXNpZHNlazN2cmswMmtqcTUuYXBwcy5nb29nbGV1c2VyY29udGVudC5jb20iLCJzdWIiOiIxMDQ3OTY4NjU4ODE0ODEwMzQzMDAiLCJlbWFpbCI6ImNvZGVob3VzZS5tYWtyb21hcGFAZ21haWwuY29tIiwiZW1haWxfdmVyaWZpZWQiOnRydWUsIm5hbWUiOiJNYWtybyBNYXBhIiwicGljdHVyZSI6Imh0dHBzOi8vbGgzLmdvb2dsZXVzZXJjb250ZW50LmNvbS9hLS9BT2gxNEdqeHpSTGlGQ09HTGdCaldnal9mbDNkM2RXSjZxOTctb2V0dll0aD1zOTYtYyIsImdpdmVuX25hbWUiOiJNYWtybyIsImZhbWlseV9uYW1lIjoiTWFwYSIsImxvY2FsZSI6InBsIiwiaWF0IjoxNTk5NTA1OTYxLCJleHAiOjE1OTk1MDk1NjF9.on4Hqew8A_qf1aHl_5pxssMNAEJ9scsmggRjTRPd9lLC_09bCrWN_TqroGiIbx6x7CzeRFSpAFN9RQclOy-UGWOxAJhYamXNkUIP7ue48kW8jRAsK0ixi6nM9tdI6zyGjElhVpF2C75Wpy8UNJWnuitwve_xqWYxvrl3Zc7oLehet_D3r3wR9W9UgqwcWeT0gHp5vJbGdfk6ckH3R2BaITs8DMZ-VA2Q40pl09L9rvY4GHloXv0Mw22T2OiEWY5--3szRm2pTC6im_EpmyA5PAftW-ZO_D2HlAH0OGJnj_GsnQmFzCzsQ111SQRI63g7H-LAUjQJAxBOE-TN0MdkmA";

  //test.makro01@gmail.com
  public static final String PREMIUM_USER_TOKEN = "eyJhbGciOiJSUzI1NiIsImtpZCI6IjNmMzMyYjNlOWI5MjhiZmU1MWJjZjRmOGRhNTQzY2M0YmQ5ZDQ3MjQiLCJ0eXAiOiJKV1QifQ.eyJpc3MiOiJodHRwczovL2FjY291bnRzLmdvb2dsZS5jb20iLCJhenAiOiI1NjQ4MTI2MDYxOTgtN2cxdnRoNHI2OGp1dHNuaDJkMnE4bDBpbWtxaW0wcXYuYXBwcy5nb29nbGV1c2VyY29udGVudC5jb20iLCJhdWQiOiI1NjQ4MTI2MDYxOTgtdTZiNXRvOHJoanNxa3BpOXNpZHNlazN2cmswMmtqcTUuYXBwcy5nb29nbGV1c2VyY29udGVudC5jb20iLCJzdWIiOiIxMTgzNjQ4NDc5MTE1MDIyMTA0MTYiLCJlbWFpbCI6InRlc3QubWFrcm8wMUBnbWFpbC5jb20iLCJlbWFpbF92ZXJpZmllZCI6dHJ1ZSwibmFtZSI6Ik1ha3JvbWFwYSBUZXN0MDEiLCJwaWN0dXJlIjoiaHR0cHM6Ly9saDMuZ29vZ2xldXNlcmNvbnRlbnQuY29tLy1pa1lfQ203MmN6dy9BQUFBQUFBQUFBSS9BQUFBQUFBQUFBQS9BTVp1dWNsT1lzLWxRbXhBV2FoSzROaHFZVS1XMnBjeTdnL3M5Ni1jL3Bob3RvLmpwZyIsImdpdmVuX25hbWUiOiJNYWtyb21hcGEiLCJmYW1pbHlfbmFtZSI6IlRlc3QwMSIsImxvY2FsZSI6InBsIiwiaWF0IjoxNTk5NTA1NTYyLCJleHAiOjE1OTk1MDkxNjJ9.SuGw2n_Ap_JM4AEQOOmZQKKMMJ3QPo9XiZlQ6_qyF-0uwh-6JH8kCApZtM5eQX1v5524B0oG2fGfsAQv5BhvADRbCwBTuSS6UKxpN8e8CLE3NbYfiutWUZEBr76xAHRzaEIeXVxV5ANcrbqT09J-y0AU4WXlNWRB8iQJbCTHoLdxitK86kHr0NGMNyhu4rAaNVZ8DzlCfYlKadQwLcpz17FrthRUBvfdffwxxkLqLjh0xdCbp5-1ma9G5o3XHARwHv4PRRqxalTKDzhr2foi-bzuZhSixf-hvTbjBKvoBYPKE3YQxwxP6-JbyXeosXUV7A82vgFMfF0b60LcJHGlQg";

  //info@code-house.pl
  public static final String APPLE_USER_TOKEN = "eyJraWQiOiJlWGF1bm1MIiwiYWxnIjoiUlMyNTYifQ.eyJpc3MiOiJodHRwczovL2FwcGxlaWQuYXBwbGUuY29tIiwiYXVkIjoicGwuY29kZWhvdXNlLm1ha3JvLm1hcGEubW9iaWxlIiwiZXhwIjoxNTk3Njk1NzA4LCJpYXQiOjE1OTc2OTUxMDgsInN1YiI6IjAwMDk2Ny5iNWI5NjU2OGE2NGM0YmQyOGFlYzU5ZDYxODYzOTllMS4wODM3IiwiY19oYXNoIjoiV1hTMUFEUFREd0lxODd2NE9PeWw2dyIsImF1dGhfdGltZSI6MTU5NzY5NTEwOCwibm9uY2Vfc3VwcG9ydGVkIjp0cnVlfQ.pptJ_oeF-AJi8f0EWbhqJCZpqvRrGjzNGH73tvcmobiJ7suEVPLBjHwFNKechx56SRQr6QgS9qoQqKw-QXgcbgZaPFvqrcyKcAb6DGhLeGAvIUh-L8a_IFyoHxdwQU0iglVXb9LSP4vzpNNUP2BWPDNucQ_Gk_kqatEUy0mRTVHfeT5gzgk1TOgsCMQDLz3cSFflktKk5jxeCFXjy-Tc2Qqk0IkwNQjfYg262HNAlrzTKFoLCr47xMvok9FCYMIJf4PsC7hpKUwFSbUx0LiG68_taRXH0dq0ODWjnxmpjk8gjQOJUKbkbQiiwNO9V843CEvJaAQfv8vsCcO89MPwiA";

  //test.makro01@gmail.com
  public static final String FACEBOOK_USER_ACCESS_CODE = "EAAIyey5GNe0BAPZBnZASUOtvQS6Nj6ne35ZCSLkie0bICWxJXO8kJs8HwcEGdjLb2ZBrum69h5H8OLwwBxaNBpwt6ZBNoHASWJZAU2Rin7yr6m8CVTmJFKGMwRCfyjd0QZCbgq3BvZBBDIiTWD4shEY1grj7nXQsHEmT9PIqZATtSwdExAYV6aRIJcZCO3l0mi9ZBqm46vkTAvHGXc9mYfz1ZBsnlRY13mIHfiIZD";

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