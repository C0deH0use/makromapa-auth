package pl.code.house.makro.mapa.auth.domain.user;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.AdditionalAnswers.returnsFirstArg;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.times;
import static pl.code.house.makro.mapa.auth.domain.user.AuthProvider.GOOGLE;
import static pl.code.house.makro.mapa.auth.domain.user.TestUser.GOOGLE_NEW_USER;
import static pl.code.house.makro.mapa.auth.domain.user.UserType.FREE_USER;

import java.time.Instant;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.oauth2.jwt.Jwt;
import pl.code.house.makro.mapa.auth.domain.token.AccessTokenFacade;
import pl.code.house.makro.mapa.auth.error.InsufficientUserDetailsException;
import pl.code.house.makro.mapa.auth.error.NewTermsAndConditionsNotApprovedException;
import pl.code.house.makro.mapa.auth.error.UnsupportedAuthenticationIssuerException;

@ExtendWith(MockitoExtension.class)
class UserTokenAuthenticationServiceTest {

  private static final String EXTERNAL_USER_ID = "109775294290602056945";

  @InjectMocks
  private UserTokenAuthenticationService sut;

  @Mock
  private UserRepository repository;

  @Mock
  private TermsAndConditionsRepository termsRepository;

  @Mock
  private AccessTokenFacade accessTokenFacade;

  @Test
  @DisplayName("authorize new principal with JWT token")
  void authorizeNewPrincipalWithJwtToken() {
    //given
    String token = GOOGLE_NEW_USER.getJwt();
    Map<String, Object> headers = tokenHeaders();
    Map<String, Object> claims = tokenClaims(GOOGLE.getIssuer());

    Jwt principal = Jwt.withTokenValue(token)
        .headers(h -> h.putAll(headers))
        .claims(c -> c.putAll(claims))
        .build();

    given(repository.findByExternalId(GOOGLE_NEW_USER.getExternalId())).willReturn(Optional.empty());
    given(repository.save(any(User.class))).willAnswer(returnsFirstArg());

    //when
    sut.authorizePrincipal(principal);

    //then
    ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);

    then(repository).should(times(1)).save(userCaptor.capture());
    User passedUser = userCaptor.getValue();
    assertThat(passedUser.getProvider()).isEqualTo(GOOGLE);
    assertThat(passedUser.getTermsAndConditionsId()).isNull();
    assertThat(passedUser.getUserDetails().getType()).isEqualTo(FREE_USER);
    assertThat(passedUser.getUserDetails().getName()).isEqualTo(GOOGLE_NEW_USER.getName());
    assertThat(passedUser.getUserDetails().getEmail()).isNotBlank();
    assertThat(passedUser.getUserDetails().getPicture()).isNotBlank();

    then(accessTokenFacade).should(times(1)).issueTokenFor(null);
  }

  @Test
  @DisplayName("authorize new principal if JWT token is missing user detail")
  void authorizeNewPrincipalIfJwtTokenIsMissingUserDetail() {
    //given
    String token = GOOGLE_NEW_USER.getJwt();
    Map<String, Object> headers = tokenHeaders();
    Map<String, Object> claims = minimalClaims();

    Jwt principal = Jwt.withTokenValue(token)
        .headers(h -> h.putAll(headers))
        .claims(c -> c.putAll(claims))
        .build();

    given(repository.findByExternalId(GOOGLE_NEW_USER.getExternalId())).willReturn(Optional.empty());
    given(repository.save(any(User.class))).willAnswer(returnsFirstArg());

    //when
    sut.authorizePrincipal(principal);

    //then
    ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);

    then(repository).should(times(1)).save(userCaptor.capture());
    User passedUser = userCaptor.getValue();
    assertThat(passedUser.getProvider()).isEqualTo(GOOGLE);
    assertThat(passedUser.getTermsAndConditionsId()).isNull();
    assertThat(passedUser.getUserDetails().getType()).isEqualTo(FREE_USER);
    assertThat(passedUser.getUserDetails().getName()).isNull();
    assertThat(passedUser.getUserDetails().getEmail()).isNull();
    assertThat(passedUser.getUserDetails().getPicture()).isNull();

    then(accessTokenFacade).should(times(1)).issueTokenFor(null);
  }

  @Test
  @DisplayName("authorize JWT token of existing PREMIUM User")
  void authorizeJwtTokenOfExistingPremiumUser() {
    //given
    String token = GOOGLE_NEW_USER.getJwt();
    Map<String, Object> headers = tokenHeaders();
    Map<String, Object> claims = minimalClaims();

    Jwt principal = Jwt.withTokenValue(token)
        .headers(h -> h.putAll(headers))
        .claims(c -> c.putAll(claims))
        .build();

    User premiumUser = User.builder()
        .id(1000L)
        .externalId(EXTERNAL_USER_ID)
        .provider(GOOGLE)
        .termsAndConditionsId(1000L)
        .userDetails(UserDetails.builder()
            .type(UserType.PREMIUM_USER)
            .build())
        .build();

    TermsAndConditions currentTnC = TermsAndConditions.builder().id(1000L).build();

    given(repository.findByExternalId(GOOGLE_NEW_USER.getExternalId())).willReturn(Optional.of(premiumUser));
    given(termsRepository.findFirstByOrderByLastUpdatedDesc()).willReturn(currentTnC);

    //when
    sut.authorizePrincipal(principal);

    //then
    ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);

    then(repository).should(times(0)).save(userCaptor.capture());
    then(accessTokenFacade).should(times(1)).issueTokenFor(1000L);
  }

  @Test
  @DisplayName("throw if unknown issuer")
  void throwIfUnknownIssuer() {
    //given
    String token = GOOGLE_NEW_USER.getJwt();
    Map<String, Object> headers = tokenHeaders();
    Map<String, Object> claims = tokenClaims("https://unknown-issuer.com");

    Jwt principal = Jwt.withTokenValue(token)
        .headers(h -> h.putAll(headers))
        .claims(c -> c.putAll(claims))
        .build();

    //then
    assertThatThrownBy(() -> sut.authorizePrincipal(principal))
        .isInstanceOf(UnsupportedAuthenticationIssuerException.class)
        .hasMessageContaining("Unknown issuer")
        .hasMessageContaining("https://unknown-issuer.com")
    ;
  }

  @Test
  @DisplayName("throw if token has unknown user id property mapping")
  void throwIfTokenHasUnknownUserIdPropertyMapping() {
    //given
    String token = GOOGLE_NEW_USER.getJwt();
    Map<String, Object> headers = tokenHeaders();
    Map<String, Object> claims = customTokenClaims();

    Jwt principal = Jwt.withTokenValue(token)
        .headers(h -> h.putAll(headers))
        .claims(c -> c.putAll(claims))
        .build();

    //then
    assertThatThrownBy(() -> sut.authorizePrincipal(principal))
        .isInstanceOf(InsufficientUserDetailsException.class)
        .hasMessageContaining("external user Id is missing")
    ;
  }

  @Test
  @DisplayName("throw when user needs to approve new TermsAndConditions to get authorized")
  void throwWhenUserNeedsToApproveNewTermsAndConditionsToGetAuthorized() {
    //given
    String token = GOOGLE_NEW_USER.getJwt();
    Map<String, Object> headers = tokenHeaders();
    Map<String, Object> claims = minimalClaims();

    Jwt principal = Jwt.withTokenValue(token)
        .headers(h -> h.putAll(headers))
        .claims(c -> c.putAll(claims))
        .build();

    User premiumUser = User.builder()
        .id(1000L)
        .externalId(EXTERNAL_USER_ID)
        .provider(GOOGLE)
        .termsAndConditionsId(1000L)
        .userDetails(UserDetails.builder()
            .type(UserType.PREMIUM_USER)
            .build())
        .build();

    TermsAndConditions currentTnC = TermsAndConditions.builder().id(1001L).build();

    given(repository.findByExternalId(GOOGLE_NEW_USER.getExternalId())).willReturn(Optional.of(premiumUser));
    given(termsRepository.findFirstByOrderByLastUpdatedDesc()).willReturn(currentTnC);

    //when
    assertThatThrownBy(() -> sut.authorizePrincipal(principal))
        .isInstanceOf(NewTermsAndConditionsNotApprovedException.class)
        .hasMessageContaining("New terms and conditions")
    ;
  }

  private Map<String, Object> tokenClaims(String issuer) {
    return Map.ofEntries(
        Map.entry("sub", EXTERNAL_USER_ID),
        Map.entry("email_verified", true),
        Map.entry("iss", issuer),
        Map.entry("given_name", "Test"),
        Map.entry("locale", "pl"),
        Map.entry("picture", "https://lh4.googleusercontent.com/-zRLIMvC5Mtg/AAAAAAAAAAI/AAAAAAAAAAA/AMZuuckOdTV-W5W9ytoYjXw3Ojp-BfZ0Sg/s96-c/photo.jpg"),
        Map.entry("aud", 1),
        Map.entry("azp", "564812606198-7g1vth4r68jutsnh2d2q8l0imkqim0qv.apps.googleusercontent.com"),
        Map.entry("name", "Test Android1"),
        Map.entry("exp", Instant.parse("2020-07-11T17:20:58Z")),
        Map.entry("family_name", "Android1"),
        Map.entry("iat", Instant.parse("2020-07-11T16:20:58Z")),
        Map.entry("email", "andr0id1makr0mapa@gmail.com")
    );
  }

  private Map<String, Object> minimalClaims() {
    return Map.ofEntries(
        Map.entry("sub", EXTERNAL_USER_ID),
        Map.entry("iss", "https://accounts.google.com"),
        Map.entry("azp", "564812606198-7g1vth4r68jutsnh2d2q8l0imkqim0qv.apps.googleusercontent.com"),
        Map.entry("exp", Instant.parse("2020-07-11T17:20:58Z")),
        Map.entry("iat", Instant.parse("2020-07-11T16:20:58Z"))
    );
  }

  private Map<String, Object> customTokenClaims() {
    return Map.ofEntries(
        Map.entry("user_id", EXTERNAL_USER_ID)
    );
  }

  private Map<String, Object> tokenHeaders() {
    return Map.of(
        "kid", "65b3feaad9db0f38b1b4ab94553ff17de4dd4d49",
        "typ", "JWT",
        "alg", "RS256"
    );
  }

}