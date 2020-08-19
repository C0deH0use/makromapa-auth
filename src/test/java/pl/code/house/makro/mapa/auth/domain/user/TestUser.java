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
  public static final String NEW_USER_TOKEN = "eyJhbGciOiJSUzI1NiIsImtpZCI6IjZiYzYzZTlmMThkNTYxYjM0ZjU2NjhmODhhZTI3ZDQ4ODc2ZDgwNzMiLCJ0eXAiOiJKV1QifQ.eyJpc3MiOiJodHRwczovL2FjY291bnRzLmdvb2dsZS5jb20iLCJhenAiOiI1NjQ4MTI2MDYxOTgtcHNqbzAwamJ1bWFzMTE3cGV1YjVoMzN2ODN0NGozaXAuYXBwcy5nb29nbGV1c2VyY29udGVudC5jb20iLCJhdWQiOiI1NjQ4MTI2MDYxOTgtcHNqbzAwamJ1bWFzMTE3cGV1YjVoMzN2ODN0NGozaXAuYXBwcy5nb29nbGV1c2VyY29udGVudC5jb20iLCJzdWIiOiIxMDQ3OTY4NjU4ODE0ODEwMzQzMDAiLCJlbWFpbCI6ImNvZGVob3VzZS5tYWtyb21hcGFAZ21haWwuY29tIiwiZW1haWxfdmVyaWZpZWQiOnRydWUsImF0X2hhc2giOiI0WGRkRDc5bnBYUDd2TjR3ZGFjYUlnIiwibm9uY2UiOiJrQ0pkM0RSaTJ4Z3l5emw2T1dxdDJ6R1pncnBjem9RMk83eVVZWXFqdVpBIiwibmFtZSI6Ik1ha3JvIE1hcGEiLCJwaWN0dXJlIjoiaHR0cHM6Ly9saDMuZ29vZ2xldXNlcmNvbnRlbnQuY29tL2EtL0FPaDE0R2p4elJMaUZDT0dMZ0JqV2dqX2ZsM2QzZFdKNnE5Ny1vZXR2WXRoPXM5Ni1jIiwiZ2l2ZW5fbmFtZSI6Ik1ha3JvIiwiZmFtaWx5X25hbWUiOiJNYXBhIiwibG9jYWxlIjoicGwiLCJpYXQiOjE1OTc1OTE4NDgsImV4cCI6MTU5NzU5NTQ0OH0.DVZjvyqrcTeBBduGVyNdTaMwPwTrXu7JaUJn0B226-xfTVgPQTj6usf2Dv6kVcXg5sAAbfxFRMBB5lMTOrb2jwQTgTX7U--7rFcyOkqdElvgyXviei24O4uLX8Q1LWie2IxwVgE7xFYcr5hdBUXNXY1CjX_3yu5oJv1ZLPxMTvoa95_ZEUvYnWTHLh_wXozCyKlW195lkdj1bqhnMAwmmRALfVjmkEbgNOy4s27YGC9XVf7cSeq7lHKWvelYXjbPipE5JBkq4iBsLAMtL5DxCnIQwy-RGVcq6avGAerFn-4cVMm8WEnNSz_wGMz38veO2RLNTF0S19mRbs__dM6BDw";

  //test.makro01@gmail.com
  public static final String PREMIUM_USER_TOKEN = "eyJhbGciOiJSUzI1NiIsImtpZCI6IjZiYzYzZTlmMThkNTYxYjM0ZjU2NjhmODhhZTI3ZDQ4ODc2ZDgwNzMiLCJ0eXAiOiJKV1QifQ.eyJpc3MiOiJodHRwczovL2FjY291bnRzLmdvb2dsZS5jb20iLCJhenAiOiI1NjQ4MTI2MDYxOTgtcHNqbzAwamJ1bWFzMTE3cGV1YjVoMzN2ODN0NGozaXAuYXBwcy5nb29nbGV1c2VyY29udGVudC5jb20iLCJhdWQiOiI1NjQ4MTI2MDYxOTgtcHNqbzAwamJ1bWFzMTE3cGV1YjVoMzN2ODN0NGozaXAuYXBwcy5nb29nbGV1c2VyY29udGVudC5jb20iLCJzdWIiOiIxMTgzNjQ4NDc5MTE1MDIyMTA0MTYiLCJlbWFpbCI6InRlc3QubWFrcm8wMUBnbWFpbC5jb20iLCJlbWFpbF92ZXJpZmllZCI6dHJ1ZSwiYXRfaGFzaCI6IlRCc2ZnUkdXUEdVWkJBM0FCaGx3VEEiLCJub25jZSI6Imw1dnRqbFhWby0yOFl2SjFEQXU5RUY0Y2ZiRWo1cmpNa1I1ZGt4UC02SXciLCJuYW1lIjoiTWFrcm9tYXBhIFRlc3QwMSIsInBpY3R1cmUiOiJodHRwczovL2xoMy5nb29nbGV1c2VyY29udGVudC5jb20vLWlrWV9DbTcyY3p3L0FBQUFBQUFBQUFJL0FBQUFBQUFBQUFBL0FNWnV1Y2xPWXMtbFFteEFXYWhLNE5ocVlVLVcycGN5N2cvczk2LWMvcGhvdG8uanBnIiwiZ2l2ZW5fbmFtZSI6Ik1ha3JvbWFwYSIsImZhbWlseV9uYW1lIjoiVGVzdDAxIiwibG9jYWxlIjoicGwiLCJpYXQiOjE1OTc1OTE5MTEsImV4cCI6MTU5NzU5NTUxMX0.cDFCkSsVcqnomXSizQGyRYYa-fLcazF6s9tB-u3TdaDARS4p4JGI4797Jxt71grWOh42uMnX1ojmvJJg1GUrUqtP6jMRnSzP7Qx2rt87PAFxYBY1bBSYCTxPuNGbO-yM5v6XnFI2JQpVNuJmyYMzLuJcblzdO2Avcs8YgtTR4R9fiIQtShnITg5SOQMvSuzwYy8m29_XfOfst8HE2r5X1Z4sWGk8YbCMS_4n4iYHnKbbiu0UUpQKUUCVNRLRXMpGtKVAWUGNPPrSpXegifE9FMym-Vbu42eS4PZw_mT5n0yTjNvWp7l0-z0j4a8fZeGadqoBJCkRmeWlYxwiE33B3Q";

  //info@code-house.pl
  public static final String APPLE_USER_TOKEN = "eyJraWQiOiJlWGF1bm1MIiwiYWxnIjoiUlMyNTYifQ.eyJpc3MiOiJodHRwczovL2FwcGxlaWQuYXBwbGUuY29tIiwiYXVkIjoicGwuY29kZWhvdXNlLm1ha3JvLm1hcGEubW9iaWxlIiwiZXhwIjoxNTk3Njk1NzA4LCJpYXQiOjE1OTc2OTUxMDgsInN1YiI6IjAwMDk2Ny5iNWI5NjU2OGE2NGM0YmQyOGFlYzU5ZDYxODYzOTllMS4wODM3IiwiY19oYXNoIjoiV1hTMUFEUFREd0lxODd2NE9PeWw2dyIsImF1dGhfdGltZSI6MTU5NzY5NTEwOCwibm9uY2Vfc3VwcG9ydGVkIjp0cnVlfQ.pptJ_oeF-AJi8f0EWbhqJCZpqvRrGjzNGH73tvcmobiJ7suEVPLBjHwFNKechx56SRQr6QgS9qoQqKw-QXgcbgZaPFvqrcyKcAb6DGhLeGAvIUh-L8a_IFyoHxdwQU0iglVXb9LSP4vzpNNUP2BWPDNucQ_Gk_kqatEUy0mRTVHfeT5gzgk1TOgsCMQDLz3cSFflktKk5jxeCFXjy-Tc2Qqk0IkwNQjfYg262HNAlrzTKFoLCr47xMvok9FCYMIJf4PsC7hpKUwFSbUx0LiG68_taRXH0dq0ODWjnxmpjk8gjQOJUKbkbQiiwNO9V843CEvJaAQfv8vsCcO89MPwiA";

  // -- External(JWT) UserDetails
  public static ExternalMockUser GOOGLE_NEW_USER = new ExternalMockUser(null, "Makro Mapa", "104796865881481034300", NEW_USER_TOKEN, null);
  public static ExternalMockUser GOOGLE_PREMIUM_USER = new ExternalMockUser("aa6641c1-e9f4-417f-adf4-f71accc470cb", "Makromapa Test01", "118364847911502210416", PREMIUM_USER_TOKEN,
      "b146b422-475c-4beb-9e9c-4e33e2288b08");

  public static ExternalMockUser APPLE_NEW_USER = new ExternalMockUser(null, null, "000967.b5b96568a64c4bd28aec59d6186399e1.0837", APPLE_USER_TOKEN, null);


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