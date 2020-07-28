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
  public static final String NEW_USER_TOKEN = "eyJhbGciOiJSUzI1NiIsImtpZCI6ImI2M2VlMGJlMDkzZDliYzMxMmQ5NThjOTk2NmQyMWYwYzhmNmJiYmIiLCJ0eXAiOiJKV1QifQ.eyJpc3MiOiJodHRwczovL2FjY291bnRzLmdvb2dsZS5jb20iLCJhenAiOiI1NjQ4MTI2MDYxOTgtN2cxdnRoNHI2OGp1dHNuaDJkMnE4bDBpbWtxaW0wcXYuYXBwcy5nb29nbGV1c2VyY29udGVudC5jb20iLCJhdWQiOiI1NjQ4MTI2MDYxOTgtdTZiNXRvOHJoanNxa3BpOXNpZHNlazN2cmswMmtqcTUuYXBwcy5nb29nbGV1c2VyY29udGVudC5jb20iLCJzdWIiOiIxMDQ3OTY4NjU4ODE0ODEwMzQzMDAiLCJlbWFpbCI6ImNvZGVob3VzZS5tYWtyb21hcGFAZ21haWwuY29tIiwiZW1haWxfdmVyaWZpZWQiOnRydWUsIm5hbWUiOiJNYWtybyBNYXBhIiwicGljdHVyZSI6Imh0dHBzOi8vbGgzLmdvb2dsZXVzZXJjb250ZW50LmNvbS9hLS9BT2gxNEdqeHpSTGlGQ09HTGdCaldnal9mbDNkM2RXSjZxOTctb2V0dll0aD1zOTYtYyIsImdpdmVuX25hbWUiOiJNYWtybyIsImZhbWlseV9uYW1lIjoiTWFwYSIsImxvY2FsZSI6InBsIiwiaWF0IjoxNTk1Njg4MTc5LCJleHAiOjE1OTU2OTE3Nzl9.1zcBaU-C0Gi-u9EQ2_gfUeejOyb_gTgfWJx0HGfhGUegaYs_r_Fw186wDySnUk_GkcX0Vf-amonkXMI3e_CzMYoR79ANXk76NcokNreglmGdVuWRJyxBZU-phoHJp_YH_JP9YbNleEw07Vxvcl06KnHKkoMNoPJBWEOBqbd_KwAwCmhi1ncOlsawmRuTB4WoHfvOJzMCg2ZOZLz9E2fY0Xy_k1yhkV-ithKtatx-FQ4b6lYKkNOVQW6q-Osio9kUXfNgdLBqMQn-3zyAUU3zE9nysLlhCi9xy7hx3v6gDrmjASFrhegTEkitjL1AS79p1mPRdKN91VIDZfoD2UcfTQ";

  //test.makro01@gmail.com
  public static final String PREMIUM_USER_TOKEN = "eyJhbGciOiJSUzI1NiIsImtpZCI6ImI2M2VlMGJlMDkzZDliYzMxMmQ5NThjOTk2NmQyMWYwYzhmNmJiYmIiLCJ0eXAiOiJKV1QifQ.eyJpc3MiOiJodHRwczovL2FjY291bnRzLmdvb2dsZS5jb20iLCJhenAiOiI1NjQ4MTI2MDYxOTgtN2cxdnRoNHI2OGp1dHNuaDJkMnE4bDBpbWtxaW0wcXYuYXBwcy5nb29nbGV1c2VyY29udGVudC5jb20iLCJhdWQiOiI1NjQ4MTI2MDYxOTgtdTZiNXRvOHJoanNxa3BpOXNpZHNlazN2cmswMmtqcTUuYXBwcy5nb29nbGV1c2VyY29udGVudC5jb20iLCJzdWIiOiIxMTgzNjQ4NDc5MTE1MDIyMTA0MTYiLCJlbWFpbCI6InRlc3QubWFrcm8wMUBnbWFpbC5jb20iLCJlbWFpbF92ZXJpZmllZCI6dHJ1ZSwibmFtZSI6Ik1ha3JvbWFwYSBUZXN0MDEiLCJwaWN0dXJlIjoiaHR0cHM6Ly9saDMuZ29vZ2xldXNlcmNvbnRlbnQuY29tLy1pa1lfQ203MmN6dy9BQUFBQUFBQUFBSS9BQUFBQUFBQUFBQS9BTVp1dWNsT1lzLWxRbXhBV2FoSzROaHFZVS1XMnBjeTdnL3M5Ni1jL3Bob3RvLmpwZyIsImdpdmVuX25hbWUiOiJNYWtyb21hcGEiLCJmYW1pbHlfbmFtZSI6IlRlc3QwMSIsImxvY2FsZSI6InBsIiwiaWF0IjoxNTk1Njg2MTkyLCJleHAiOjE1OTU2ODk3OTJ9.S7Xri-4ZUUxFAKXWe_Ob44O6a-Ja0nj5Ruif0onYKzuY7rEa1SlOzMHOtSc5IUpFbX7LGREqFzXl-XfsW0Qst8HDzdqorXDBGMxv0TJ_qHrOYsLUklaOK7CM0t_Tf80DXKyFuSXqsucaohCoPWuIGNl-mkXijo4BMxxcKn7H-3Fl2vOM8IwTm1KblD8Yn03nQwwQS51dLYHwUZd44TwLBUg6kHrneSGKEpNsdRuYZIN2aPi3Dw7O3z07oM9dn4dDadfl692pI-pcX1AC_p_Wm85xOFiUOkWXQFevGzNqOM2Kx0ZymPScRM5rcGhEY-mhXVlF8nCy-jB_BlSBKf6lYA";

  //Marek.Malik@itds.pl
  public static final String APPLE_USER_TOKEN = "eyJraWQiOiI4NkQ4OEtmIiwiYWxnIjoiUlMyNTYifQ.eyJpc3MiOiJodHRwczovL2FwcGxlaWQuYXBwbGUuY29tIiwiYXVkIjoicGwuY29kZWhvdXNlLm1ha3JvLm1hcGEubW9iaWxlIiwiZXhwIjoxNTk1Mjc1OTAyLCJpYXQiOjE1OTUyNzUzMDIsInN1YiI6IjAwMDgwNy40ZjkxMWNkNmUwM2E0ZWJiOTMzYzRlMDk5YTdhZDcxMC4yMTU4IiwiY19oYXNoIjoickZRU3dOZ0l0WFZKdDBBdlZqOUxLdyIsImF1dGhfdGltZSI6MTU5NTI3NTMwMiwibm9uY2Vfc3VwcG9ydGVkIjp0cnVlfQ.XC3BT4IKE-oprB54dN2gB2Yttw6KLe6r9lvZ3jgi-ywmkF5BwiEUh0qZ8GCV-3jqLmoOqJa9YfsemYHWJq-tXG243mI8e0cVZNmXxO3ot6AzwABU0d1r06SaPl89pSHRJp5LcuWLl2sJb3OQley1rcWgZyE9ZAquwdCpA7J0U76VB1yY9G6z5wxkc0TiXqHsNYkXt_wDZ-Ipl-F4gN4eD5WzaLHnOSUD4D6lcrMNEzLwrfdJbqkHHNXKmtKErJMjLkQBuP1a3TisDr-I-Wx4yVxwH4YtNJA_AK0kF1gA6OOS0D3YOpZIn9IXNzwx1cEmWAo-Lo8RkJpsUygeJ4Nz7A";

  // -- External(JWT) UserDetails
  public static ExternalMockUser GOOGLE_NEW_USER = new ExternalMockUser(null, "Makro Mapa", "104796865881481034300", NEW_USER_TOKEN, null);
  public static ExternalMockUser GOOGLE_PREMIUM_USER = new ExternalMockUser("aa6641c1-e9f4-417f-adf4-f71accc470cb", "Makromapa Test01", "118364847911502210416", PREMIUM_USER_TOKEN,
      "b146b422-475c-4beb-9e9c-4e33e2288b08");

  public static ExternalMockUser APPLE_NEW_USER = new ExternalMockUser(null, null, "000807.4f911cd6e03a4ebb933c4e099a7ad710.2158", APPLE_USER_TOKEN, null);


  // -- Email&Password UserDetails
  public static PasswordMockUser NEW_REG_USER = new PasswordMockUser(null, "new_user@domain.com", "secret", null);
  public static PasswordMockUser REG_USER = new PasswordMockUser("96718946-4ebd-4637-ae02-5cf2b5bc1bb2", "user_1@example.com", "secret", null);

  public static PasswordMockUser REG_DRAFT_USER = new PasswordMockUser("96718946-4ebd-4637-ae02-5cf2b5bc1bb2", "draft_user_1@email.pl", "secret", "03c88cfd49");
  public static PasswordMockUser REG_DRAFT_USER_WITH_EXPIRED_CODE = new PasswordMockUser("876003b2-2454-47b1-8145-0037e7069178", "draft_user_2@email.pl", "secret", "S0905WDgha");
  public static PasswordMockUser REG_DRAFT_USER_WITH_DISABLED_CODE = new PasswordMockUser("0385d294-acf8-474b-b265-102f76ef9ae2", "draft_user_3@email.pl", "secret", "HHEH8x01B6");

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