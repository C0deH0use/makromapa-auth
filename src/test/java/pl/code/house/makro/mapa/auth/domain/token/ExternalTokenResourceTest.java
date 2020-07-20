package pl.code.house.makro.mapa.auth.domain.token;

import static org.hamcrest.Matchers.containsStringIgnoringCase;
import static org.hamcrest.Matchers.equalToIgnoringCase;
import static org.hamcrest.Matchers.notNullValue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.springframework.http.HttpHeaders.AUTHORIZATION;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static pl.code.house.makro.mapa.auth.ApiConstraints.EXTERNAL_AUTH_BASE_PATH;
import static pl.code.house.makro.mapa.auth.domain.user.TestUser.GOOGLE_NEW_USER;
import static pl.code.house.makro.mapa.auth.domain.user.TestUser.GOOGLE_PREMIUM_USER;

import io.restassured.http.Header;
import java.util.UUID;
import javax.sql.DataSource;
import lombok.SneakyThrows;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.oauth2.common.DefaultOAuth2AccessToken;
import org.springframework.security.oauth2.common.OAuth2AccessToken;
import org.springframework.security.oauth2.provider.ClientDetails;
import org.springframework.security.oauth2.provider.ClientDetailsService;
import org.springframework.security.oauth2.provider.TokenRequest;
import org.springframework.security.oauth2.provider.client.BaseClientDetails;
import org.springframework.test.web.servlet.MockMvc;
import pl.code.house.makro.mapa.auth.configuration.ImportTestAuthorizationConfig;

@ImportTestAuthorizationConfig
@ExtendWith(MockitoExtension.class)
@WebMvcTest(controllers = {ExternalTokenResource.class})
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

  @Test
  @DisplayName("return OK with new token")
  void returnOkWithNewToken() throws Exception {
    //given
    given(clientDetails.loadClientByClientId(CLIENT_ID)).willReturn(CLIENT_DETAILS);
    given(tokenGranter.grant(eq(EXTERNAL_TOKEN_TYPE), any(TokenRequest.class))).willReturn(TOKEN);

    Header authenticationHeader = GOOGLE_NEW_USER.getAuthenticationHeader();

    //when
    mvc.perform(post(EXTERNAL_AUTH_BASE_PATH + "/token")
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
  @SneakyThrows
  @DisplayName("return UNAUTHORIZED if loaded client details does not match by ID")
  void returnUnauthorizedIfLoadedClientDetailsDoesNotMatchById() {
    given(clientDetails.loadClientByClientId(CLIENT_ID)).willReturn(CLIENT_DETAILS);
    given(tokenGranter.grant(eq(EXTERNAL_TOKEN_TYPE), any(TokenRequest.class))).willReturn(TOKEN);

    Header authenticationHeader = GOOGLE_NEW_USER.getAuthenticationHeader();

    //when
    mvc.perform(post(EXTERNAL_AUTH_BASE_PATH + "/token")
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
  @SneakyThrows
  @DisplayName("return BAD if uploaded client details do not match what was send to resource")
  void returnBadIfUploadedClientDetailsDoNotMatchWhatWasSendToResource() {
    given(clientDetails.loadClientByClientId(CLIENT_ID)).willReturn(INVALID_CLIENT_DETAILS);
    given(tokenGranter.grant(eq(EXTERNAL_TOKEN_TYPE), any(TokenRequest.class))).willReturn(TOKEN);

    Header authenticationHeader = GOOGLE_NEW_USER.getAuthenticationHeader();

    //when
    mvc.perform(post(EXTERNAL_AUTH_BASE_PATH + "/token")
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
  @SneakyThrows
  @DisplayName("return BAD_REQUEST if missing grant_type param")
  void returnBadRequestIfMissingGrantTypeParam() {
    given(clientDetails.loadClientByClientId(CLIENT_ID)).willReturn(CLIENT_DETAILS);
    given(tokenGranter.grant(eq(EXTERNAL_TOKEN_TYPE), any(TokenRequest.class))).willReturn(TOKEN);

    Header authenticationHeader = GOOGLE_NEW_USER.getAuthenticationHeader();

    //when
    mvc.perform(post(EXTERNAL_AUTH_BASE_PATH + "/token")
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
  @SneakyThrows
  @DisplayName("return BAD_REQUEST if grant_type is unknown or not supported")
  void returnBadRequestIfGrantTypeIsUnknownOrNotSupported() {
    //given
    given(clientDetails.loadClientByClientId(CLIENT_ID)).willReturn(CLIENT_DETAILS);
    given(tokenGranter.grant(eq(EXTERNAL_TOKEN_TYPE), any(TokenRequest.class))).willReturn(null);

    Header authenticationHeader = GOOGLE_NEW_USER.getAuthenticationHeader();

    //when
    mvc.perform(post(EXTERNAL_AUTH_BASE_PATH + "/token")
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
}