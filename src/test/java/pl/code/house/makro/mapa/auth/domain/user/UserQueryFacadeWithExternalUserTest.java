package pl.code.house.makro.mapa.auth.domain.user;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.when;
import static pl.code.house.makro.mapa.auth.domain.user.OAuth2Provider.GOOGLE;
import static pl.code.house.makro.mapa.auth.domain.user.TestUser.GOOGLE_NEW_USER;
import static pl.code.house.makro.mapa.auth.domain.user.TestUser.GOOGLE_PREMIUM_USER;
import static pl.code.house.makro.mapa.auth.domain.user.UserType.FREE_USER;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jwt.Jwt;
import pl.code.house.makro.mapa.auth.domain.user.TestUser.ExternalMockUser;
import pl.code.house.makro.mapa.auth.error.InsufficientUserDetailsException;
import pl.code.house.makro.mapa.auth.error.NewTermsAndConditionsNotApprovedException;
import pl.code.house.makro.mapa.auth.error.UnsupportedAuthenticationIssuerException;

@ExtendWith(MockitoExtension.class)
class UserQueryFacadeWithExternalUserTest {

  @Mock
  private UserRepository repository;

  @Mock
  private TermsAndConditionsFacade termsAndConditionsFacade;

  @Spy
  private PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

  @Mock
  private VerificationCodeService activationCodeService;

  @Mock
  private UserAuthoritiesService userAuthoritiesService;

  @Mock
  private ExternalUser storedUser;

  @InjectMocks
  private UserQueryFacade sut;

  @Test
  @DisplayName("authorize new principal with JWT token")
  void authorizeNewPrincipalWithJwtToken() {
    //given
    String token = GOOGLE_NEW_USER.getJwt();
    Map<String, Object> headers = tokenHeaders();
    Map<String, Object> claims = tokenClaims(GOOGLE.getIssuer(), GOOGLE_NEW_USER);

    Jwt principal = Jwt.withTokenValue(token)
        .headers(h -> h.putAll(headers))
        .claims(c -> c.putAll(claims))
        .build();

    given(repository.findByExternalIdAndAuthProvider(GOOGLE_NEW_USER.getExternalId(), GOOGLE)).willReturn(Optional.empty());
    given(userAuthoritiesService.getUserAuthorities(GOOGLE_NEW_USER.getUserId())).willReturn(List.of());

    given(storedUser.getId()).willReturn(GOOGLE_NEW_USER.getUserId());
    given(repository.saveAndFlush(any(BaseUser.class))).willReturn(storedUser);

    //when
    sut.findUserByToken(principal);

    //then
    ArgumentCaptor<BaseUser> userCaptor = ArgumentCaptor.forClass(BaseUser.class);

    then(repository).should(times(1)).saveAndFlush(userCaptor.capture());
    BaseUser passedUser = userCaptor.getValue();
    assertThat(passedUser.getProvider()).isEqualTo(GOOGLE);
    assertThat(passedUser.getTermsAndConditionsId()).isNull();
    assertThat(passedUser.getUserDetails().getType()).isEqualTo(FREE_USER);
    assertThat(passedUser.getUserDetails().getName()).isEqualTo(GOOGLE_NEW_USER.getName());
    assertThat(passedUser.getUserDetails().getEmail()).isNotBlank();
    assertThat(passedUser.getUserDetails().getPicture()).isBlank();
  }

  @Test
  @DisplayName("authorize new principal if JWT token is missing user detail")
  void authorizeNewPrincipalIfJwtTokenIsMissingUserDetail() {
    //given
    String token = GOOGLE_NEW_USER.getJwt();
    Map<String, Object> headers = tokenHeaders();
    Map<String, Object> claims = minimalClaims(GOOGLE_NEW_USER.getExternalId());

    Jwt principal = Jwt.withTokenValue(token)
        .headers(h -> h.putAll(headers))
        .claims(c -> c.putAll(claims))
        .build();

    given(repository.findByExternalIdAndAuthProvider(GOOGLE_NEW_USER.getExternalId(), GOOGLE)).willReturn(Optional.empty());
    given(userAuthoritiesService.getUserAuthorities(GOOGLE_NEW_USER.getUserId())).willReturn(List.of());

    given(storedUser.getId()).willReturn(GOOGLE_NEW_USER.getUserId());
    given(repository.saveAndFlush(any(BaseUser.class))).willReturn(storedUser);

    //when
    sut.findUserByToken(principal);

    //then
    ArgumentCaptor<BaseUser> userCaptor = ArgumentCaptor.forClass(BaseUser.class);

    then(repository).should(times(1)).saveAndFlush(userCaptor.capture());
    BaseUser passedUser = userCaptor.getValue();
    assertThat(passedUser.getProvider()).isEqualTo(GOOGLE);
    assertThat(passedUser.getTermsAndConditionsId()).isNull();
    assertThat(passedUser.getUserDetails().getType()).isEqualTo(FREE_USER);
    assertThat(passedUser.getUserDetails().getName()).isNull();
    assertThat(passedUser.getUserDetails().getEmail()).isNull();
    assertThat(passedUser.getUserDetails().getPicture()).isNull();
  }

  @Test
  @DisplayName("authorize JWT token of existing PREMIUM User")
  void authorizeJwtTokenOfExistingPremiumUser() {
    //given
    String token = GOOGLE_NEW_USER.getJwt();
    Map<String, Object> headers = tokenHeaders();
    Map<String, Object> claims = minimalClaims(GOOGLE_PREMIUM_USER.getExternalId());

    Jwt principal = Jwt.withTokenValue(token)
        .headers(h -> h.putAll(headers))
        .claims(c -> c.putAll(claims))
        .build();

    ExternalUser premiumUser = new ExternalUser(
        GOOGLE_PREMIUM_USER.getUserId(),
        1000L,
        GOOGLE,
        UserDetails.builder().type(FREE_USER).build(),
        GOOGLE_PREMIUM_USER.getExternalId(),
        true
    );

    TermsAndConditions currentTnC = TermsAndConditions.builder().id(1000L).build();

    given(repository.findByExternalIdAndAuthProvider(GOOGLE_PREMIUM_USER.getExternalId(), GOOGLE)).willReturn(Optional.of(premiumUser));
    given(userAuthoritiesService.getUserAuthorities(GOOGLE_PREMIUM_USER.getUserId())).willReturn(List.of(new SimpleGrantedAuthority("ROLE_PREMIUM")));

    //when
    sut.findUserByToken(principal);

    //then
    ArgumentCaptor<BaseUser> userCaptor = ArgumentCaptor.forClass(BaseUser.class);

    then(repository).should(times(0)).save(userCaptor.capture());
  }

  @Test
  @DisplayName("throw if unknown issuer")
  void throwIfUnknownIssuer() {
    //given
    String token = GOOGLE_NEW_USER.getJwt();
    Map<String, Object> headers = tokenHeaders();
    Map<String, Object> claims = tokenClaims("https://unknown-issuer.com", GOOGLE_PREMIUM_USER);

    Jwt principal = Jwt.withTokenValue(token)
        .headers(h -> h.putAll(headers))
        .claims(c -> c.putAll(claims))
        .build();

    //then
    assertThatThrownBy(() -> sut.findUserByToken(principal))
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
    assertThatThrownBy(() -> sut.findUserByToken(principal))
        .isInstanceOf(InsufficientUserDetailsException.class)
        .hasMessageContaining("external user Id is missing")
    ;
  }

  private Map<String, Object> tokenClaims(String issuer, ExternalMockUser user) {
    return Map.ofEntries(
        Map.entry("sub", user.getExternalId()),
        Map.entry("email_verified", true),
        Map.entry("iss", issuer),
        Map.entry("given_name", "Test"),
        Map.entry("locale", "pl"),
        Map.entry("picture", "https://lh4.googleusercontent.com/-zRLIMvC5Mtg/AAAAAAAAAAI/AAAAAAAAAAA/AMZuuckOdTV-W5W9ytoYjXw3Ojp-BfZ0Sg/s96-c/photo.jpg"),
        Map.entry("aud", 1),
        Map.entry("azp", "564812606198-7g1vth4r68jutsnh2d2q8l0imkqim0qv.apps.googleusercontent.com"),
        Map.entry("name", user.getName()),
        Map.entry("exp", Instant.parse("2020-07-11T17:20:58Z")),
        Map.entry("family_name", "Android1"),
        Map.entry("iat", Instant.parse("2020-07-11T16:20:58Z")),
        Map.entry("email", "andr0id1makr0mapa@gmail.com")
    );
  }

  private Map<String, Object> minimalClaims(String externalId) {
    return Map.ofEntries(
        Map.entry("sub", externalId),
        Map.entry("iss", "https://accounts.google.com"),
        Map.entry("azp", "564812606198-7g1vth4r68jutsnh2d2q8l0imkqim0qv.apps.googleusercontent.com"),
        Map.entry("exp", Instant.parse("2020-07-11T17:20:58Z")),
        Map.entry("iat", Instant.parse("2020-07-11T16:20:58Z"))
    );
  }

  private Map<String, Object> customTokenClaims() {
    return Map.ofEntries(
        Map.entry("user_id", GOOGLE_PREMIUM_USER.getExternalId())
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