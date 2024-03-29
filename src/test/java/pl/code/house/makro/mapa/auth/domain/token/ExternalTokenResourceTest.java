package pl.code.house.makro.mapa.auth.domain.token;

import static java.util.Collections.emptyList;
import static org.hamcrest.Matchers.containsStringIgnoringCase;
import static org.hamcrest.Matchers.equalToIgnoringCase;
import static org.hamcrest.Matchers.notNullValue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.springframework.http.HttpHeaders.AUTHORIZATION;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static org.springframework.security.core.context.SecurityContextHolder.getContext;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static pl.code.house.makro.mapa.auth.ApiConstraints.EXTERNAL_AUTHENTICATION_PATH;
import static pl.code.house.makro.mapa.auth.domain.user.TestUser.GOOGLE_NEW_USER;
import static pl.code.house.makro.mapa.auth.domain.user.TestUser.GOOGLE_PREMIUM_USER;

import io.restassured.http.Header;
import java.time.Instant;
import java.util.Map;
import java.util.UUID;
import javax.servlet.http.HttpServletRequest;
import javax.sql.DataSource;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.common.DefaultOAuth2AccessToken;
import org.springframework.security.oauth2.common.OAuth2AccessToken;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.provider.ClientDetails;
import org.springframework.security.oauth2.provider.ClientDetailsService;
import org.springframework.security.oauth2.provider.TokenRequest;
import org.springframework.security.oauth2.provider.client.BaseClientDetails;
import org.springframework.security.oauth2.provider.token.ResourceServerTokenServices;
import org.springframework.security.oauth2.provider.token.TokenStore;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.security.test.context.TestSecurityContextHolder;
import org.springframework.test.web.servlet.MockMvc;
import pl.code.house.makro.mapa.auth.configuration.ExternalAuthenticationManagerResolver;
import pl.code.house.makro.mapa.auth.configuration.ExternalProviders;
import pl.code.house.makro.mapa.auth.configuration.ImportTestAuthorizationConfig;

@ImportTestAuthorizationConfig
@ExtendWith(MockitoExtension.class)
@WebMvcTest(controllers = {ExternalTokenResource.class, ExternalProviders.class})
class ExternalTokenResourceTest {

  private static final String ACCESS_CODE = "10c7fc72-64f6-4c9a-af2b-a5d33c65ecf3";
  private static final String CLIENT_ID = "CLIENT_ID";
  private static final String EXTERNAL_TOKEN_TYPE = "external-token";
  private static final String CLIENT_GRANT_TYPES = "external-token,refresh_token";
  private static final ClientDetails CLIENT_DETAILS = new BaseClientDetails(CLIENT_ID, "makromapa-mobile", "user", CLIENT_GRANT_TYPES, "ROLE_CLIENT");
  private static final ClientDetails INVALID_CLIENT_DETAILS = new BaseClientDetails("INVALID_CLIENT_ID", "makromapa-admin", "admin", CLIENT_GRANT_TYPES, "ROLE_CLIENT, ROLE_ADMIN");

  private static final OAuth2AccessToken TOKEN = new DefaultOAuth2AccessToken(ACCESS_CODE);

  @Autowired
  private MockMvc mvc;

  @MockBean
  private ClientDetailsService clientDetails;

  @MockBean
  private ExternalUserCompositeTokenGranter tokenGranter;

  @MockBean
  private DataSource dataSource;

  @MockBean
  private TokenStore tokenStore;

  @MockBean
  private ResourceServerTokenServices tokenServices;

  @MockBean
  private ExternalAuthenticationManagerResolver managerResolver;

  @MockBean
  private AuthenticationManager authenticationManager;

  @Test
  @DisplayName("return OK with new token")
  void returnOkWithNewToken() throws Exception {
    //given
    TestSecurityContextHolder.clearContext();
    TestSecurityContextHolder.setAuthentication(validAuthentication());

    given(clientDetails.loadClientByClientId(CLIENT_ID)).willReturn(CLIENT_DETAILS);
    given(tokenGranter.grant(eq(EXTERNAL_TOKEN_TYPE), any(TokenRequest.class))).willReturn(TOKEN);
    given(managerResolver.resolve(any(HttpServletRequest.class))).willReturn(authenticationManager);
    given(authenticationManager.authenticate(any(Authentication.class))).willReturn(validAuthentication());

    Header authenticationHeader = GOOGLE_PREMIUM_USER.getAuthenticationHeader();

    //when
    mvc.perform(post(EXTERNAL_AUTHENTICATION_PATH + "/token")
        .contentType(APPLICATION_JSON)
        .param("client_id", CLIENT_ID)
        .param("grant_type", EXTERNAL_TOKEN_TYPE)
        .content(APPLICATION_JSON_VALUE)
        .header(AUTHORIZATION, authenticationHeader.getValue())
    )
        //then
        .andDo(print())
        .andExpect(status().isOk())
        .andExpect(jsonPath("token_type", equalToIgnoringCase("bearer")))
        .andExpect(jsonPath("access_token", equalToIgnoringCase(ACCESS_CODE)))
    ;
  }

  @Test
  @DisplayName("return UNAUTHORIZED if loaded client details does not match by ID")
  void returnUnauthorizedIfLoadedClientDetailsDoesNotMatchById() throws Exception {
    //given
    getContext().setAuthentication(validAuthentication());
    given(clientDetails.loadClientByClientId(CLIENT_ID)).willReturn(CLIENT_DETAILS);
    given(tokenGranter.grant(eq(EXTERNAL_TOKEN_TYPE), any(TokenRequest.class))).willReturn(TOKEN);
    given(managerResolver.resolve(any(HttpServletRequest.class))).willReturn(authenticationManager);
    given(authenticationManager.authenticate(any(Authentication.class))).willReturn(validAuthentication());

    Header authenticationHeader = GOOGLE_NEW_USER.getAuthenticationHeader();

    //when
    mvc.perform(post(EXTERNAL_AUTHENTICATION_PATH + "/token")
        .contentType(APPLICATION_JSON)
        .param("grant_type", EXTERNAL_TOKEN_TYPE)
        .content(APPLICATION_JSON_VALUE)
        .header(AUTHORIZATION, authenticationHeader.getValue())
    )
        //then
        .andDo(print())
        .andExpect(status().isUnauthorized())
        .andExpect(jsonPath("error", equalToIgnoringCase("Missing client authentication Id")))
        .andExpect(jsonPath("uniqueErrorId", notNullValue(UUID.class)))
    ;
  }

  @Test
  @DisplayName("return BAD if uploaded client details do not match what was send to resource")
  void returnBadIfUploadedClientDetailsDoNotMatchWhatWasSendToResource() throws Exception {
    //given
    getContext().setAuthentication(validAuthentication());

    given(clientDetails.loadClientByClientId(CLIENT_ID)).willReturn(INVALID_CLIENT_DETAILS);
    given(tokenGranter.grant(eq(EXTERNAL_TOKEN_TYPE), any(TokenRequest.class))).willReturn(TOKEN);
    given(managerResolver.resolve(any(HttpServletRequest.class))).willReturn(authenticationManager);
    given(authenticationManager.authenticate(any(Authentication.class))).willReturn(validAuthentication());

    Header authenticationHeader = GOOGLE_NEW_USER.getAuthenticationHeader();

    //when
    mvc.perform(post(EXTERNAL_AUTHENTICATION_PATH + "/token")
        .contentType(APPLICATION_JSON)
        .param("client_id", CLIENT_ID)
        .param("grant_type", EXTERNAL_TOKEN_TYPE)
        .content(APPLICATION_JSON_VALUE)
        .header(AUTHORIZATION, authenticationHeader.getValue())
    )
        //then
        .andDo(print())
        .andExpect(status().isUnauthorized())
        .andExpect(jsonPath("error", containsStringIgnoringCase("client ID does not match authenticated client")))
        .andExpect(jsonPath("uniqueErrorId", notNullValue(UUID.class)))
    ;
  }

  @Test
  @DisplayName("return BAD_REQUEST if missing grant_type param")
  void returnBadRequestIfMissingGrantTypeParam() throws Exception {
    //given
    getContext().setAuthentication(validAuthentication());

    given(clientDetails.loadClientByClientId(CLIENT_ID)).willReturn(CLIENT_DETAILS);
    given(tokenGranter.grant(eq(EXTERNAL_TOKEN_TYPE), any(TokenRequest.class))).willReturn(TOKEN);
    given(managerResolver.resolve(any(HttpServletRequest.class))).willReturn(authenticationManager);
    given(authenticationManager.authenticate(any(Authentication.class))).willReturn(validAuthentication());

    Header authenticationHeader = GOOGLE_NEW_USER.getAuthenticationHeader();

    //when
    mvc.perform(post(EXTERNAL_AUTHENTICATION_PATH + "/token")
        .contentType(APPLICATION_JSON)
        .param("client_id", CLIENT_ID)
        .content(APPLICATION_JSON_VALUE)
        .header(AUTHORIZATION, authenticationHeader.getValue())
    )
        //then
        .andDo(print())
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("error", containsStringIgnoringCase("Missing grant type")))
        .andExpect(jsonPath("uniqueErrorId", notNullValue(UUID.class)))
    ;
  }

  @Test
  @DisplayName("return BAD_REQUEST if grant_type is unknown or not supported")
  void returnBadRequestIfGrantTypeIsUnknownOrNotSupported() throws Exception {
    //given
    getContext().setAuthentication(validAuthentication());

    given(clientDetails.loadClientByClientId(CLIENT_ID)).willReturn(CLIENT_DETAILS);
    given(tokenGranter.grant(eq(EXTERNAL_TOKEN_TYPE), any(TokenRequest.class))).willReturn(null);
    given(managerResolver.resolve(any(HttpServletRequest.class))).willReturn(authenticationManager);
    given(authenticationManager.authenticate(any(Authentication.class))).willReturn(validAuthentication());

    Header authenticationHeader = GOOGLE_NEW_USER.getAuthenticationHeader();

    //when
    mvc.perform(post(EXTERNAL_AUTHENTICATION_PATH + "/token")
        .contentType(APPLICATION_JSON)
        .param("client_id", CLIENT_ID)
        .param("grant_type", EXTERNAL_TOKEN_TYPE)
        .content(APPLICATION_JSON_VALUE)
        .header(AUTHORIZATION, authenticationHeader.getValue())
    )
        //then
        .andDo(print())
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("error", containsStringIgnoringCase("Unsupported grant type:")))
        .andExpect(jsonPath("uniqueErrorId", notNullValue(UUID.class)))
        ;
  }

  private Authentication validAuthentication() {
    String token = GOOGLE_PREMIUM_USER.getJwt();
    Map<String, Object> headers = tokenHeaders();
    Map<String, Object> claims = minimalClaims();

    Jwt jwt = Jwt.withTokenValue(token)
        .headers(h -> h.putAll(headers))
        .claims(c -> c.putAll(claims))
        .build();
    return new JwtAuthenticationToken(jwt, emptyList());
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