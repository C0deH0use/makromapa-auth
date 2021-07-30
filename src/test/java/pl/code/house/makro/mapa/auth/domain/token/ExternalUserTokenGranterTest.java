package pl.code.house.makro.mapa.auth.domain.token;

import static java.util.Collections.emptyList;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.atMostOnce;
import static pl.code.house.makro.mapa.auth.domain.user.OAuth2Provider.GOOGLE;
import static pl.code.house.makro.mapa.auth.domain.user.TestUser.GOOGLE_PREMIUM_USER;
import static pl.code.house.makro.mapa.auth.domain.user.UserType.FREE_USER;

import java.time.Instant;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.provider.ClientDetails;
import org.springframework.security.oauth2.provider.ClientDetailsService;
import org.springframework.security.oauth2.provider.ExternalUserAuthRequest;
import org.springframework.security.oauth2.provider.TokenRequest;
import org.springframework.security.oauth2.provider.client.BaseClientDetails;
import org.springframework.security.oauth2.provider.token.AuthorizationServerTokenServices;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import pl.code.house.makro.mapa.auth.domain.user.UserAuthoritiesService;
import pl.code.house.makro.mapa.auth.domain.user.UserQueryFacade;
import pl.code.house.makro.mapa.auth.domain.user.dto.UserDetailsDto;
import pl.code.house.makro.mapa.auth.domain.user.dto.UserDto;

@ExtendWith(MockitoExtension.class)
class ExternalUserTokenGranterTest {

  private static final String CLIENT_ID = "CLIENT_ID";
  private static final String CLIENT_GRANT_TYPES = "external-token,refresh_token";
  private static final String VALID_GRANT_TYPE = ExternalUserTokenGranter.GRANT_TYPE;
  private static final ClientDetails MOCK_CLIENT_DETAILS = new BaseClientDetails(CLIENT_ID, null, null, CLIENT_GRANT_TYPES, null);

  @InjectMocks
  private ExternalUserTokenGranter sut;

  @Mock
  private UserQueryFacade queryFacade;

  @Mock
  private AuthorizationServerTokenServices tokenServices;

  @Mock
  private UserAuthoritiesService userAuthoritiesService;

  @Mock
  private ClientDetailsService clientDetailsService;

  @Test
  @DisplayName("grant token when valid request used")
  void grantTokenWhenValidRequestUsed() {
    //given
    TokenRequest request = validRequest();

    given(clientDetailsService.loadClientByClientId(CLIENT_ID)).willReturn(MOCK_CLIENT_DETAILS);
    given(userAuthoritiesService.getUserAuthorities(GOOGLE_PREMIUM_USER.getUserId())).willReturn(List.of(new SimpleGrantedAuthority("ROLE_PREMIUM")));
    given(queryFacade.findUserByToken(any())).willReturn(userDto());

    //when
    sut.grant(VALID_GRANT_TYPE, request);

    //then
    then(tokenServices).should(atMostOnce()).createAccessToken(any());
  }

  private Optional<UserDto> userDto() {
    return Optional.of(new UserDto(GOOGLE_PREMIUM_USER.getUserId(), GOOGLE_PREMIUM_USER.getExternalId(), GOOGLE, new UserDetailsDto(null, null, null, null, null, FREE_USER, 0), true));
  }

  private TokenRequest validRequest() {
    Map<String, String> requestParameters = Map.of();
    Set<String> scopes = new HashSet<>();
    Set<String> responseTypes = new HashSet<>();
    String token = GOOGLE_PREMIUM_USER.getJwt();
    Map<String, Object> headers = tokenHeaders();
    Map<String, Object> claims = minimalClaims();

    Jwt jwt = Jwt.withTokenValue(token)
        .headers(h -> h.putAll(headers))
        .claims(c -> c.putAll(claims))
        .build();
    JwtAuthenticationToken principal = new JwtAuthenticationToken(jwt, emptyList());
    return new ExternalUserAuthRequest(requestParameters, CLIENT_ID, scopes, responseTypes, principal);
  }

  private Map<String, Object> minimalClaims() {
    return Map.ofEntries(
        Map.entry("sub", GOOGLE_PREMIUM_USER.getExternalId()),
        Map.entry("iss", "https://accounts.google.com"),
        Map.entry("azp", "564812606198-7g1vth4r68jutsnh2d2q8l0imkqim0qv.apps.googleusercontent.com"),
        Map.entry("exp", Instant.parse("2020-07-11T17:20:58Z")),
        Map.entry("iat", Instant.parse("2020-07-11T16:20:58Z"))
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