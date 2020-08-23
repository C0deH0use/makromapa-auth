package pl.code.house.makro.mapa.auth.domain.token;

import static org.apache.commons.lang3.StringUtils.isBlank;
import static org.springframework.http.CacheControl.noStore;
import static org.springframework.http.HttpHeaders.PRAGMA;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static org.springframework.http.ResponseEntity.ok;
import static org.springframework.security.oauth2.common.util.OAuth2Utils.CLIENT_ID;
import static pl.code.house.makro.mapa.auth.ApiConstraints.EXTERNAL_AUTH_BASE_PATH;

import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.common.OAuth2AccessToken;
import org.springframework.security.oauth2.common.exceptions.InvalidClientException;
import org.springframework.security.oauth2.common.exceptions.InvalidRequestException;
import org.springframework.security.oauth2.common.exceptions.UnsupportedGrantTypeException;
import org.springframework.security.oauth2.provider.ClientDetails;
import org.springframework.security.oauth2.provider.ClientDetailsService;
import org.springframework.security.oauth2.provider.OAuth2RequestValidator;
import org.springframework.security.oauth2.provider.TokenRequest;
import org.springframework.security.oauth2.provider.request.DefaultOAuth2RequestValidator;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@AllArgsConstructor
@RequestMapping(produces = APPLICATION_JSON_VALUE, consumes = APPLICATION_JSON_VALUE)
class ExternalTokenResource {

  private final OAuth2RequestValidator requestValidator = new DefaultOAuth2RequestValidator();

  private final ClientDetailsService clientDetails;

  private final ExternalUserCompositeTokenGranter tokenGranter;

  @PostMapping(path = EXTERNAL_AUTH_BASE_PATH + "/token")
  ResponseEntity<OAuth2AccessToken> authorizeJwtToken(@AuthenticationPrincipal JwtAuthenticationToken principal,
      @RequestParam Map<String, String> parameters) {
    log.info("Authorizing user ['{}'] token", principal.getName());

    String clientId = getClientId(parameters);
    ClientDetails authenticatedClient = clientDetails.loadClientByClientId(clientId);

    if (!clientId.equals(authenticatedClient.getClientId())) {
      throw new InvalidClientException("Given client ID does not match authenticated client");
    }

    TokenRequest tokenRequest = externalUserAuthRequestFactory().createExternalUserAuthRequest(parameters, principal);

    requestValidator.validateScope(tokenRequest, authenticatedClient);

    if (!StringUtils.hasText(tokenRequest.getGrantType())) {
      throw new InvalidRequestException("Missing grant type");
    }

    OAuth2AccessToken tokenDto = tokenGranter.grant(tokenRequest.getGrantType(), tokenRequest);

    if (tokenDto == null) {
      throw new UnsupportedGrantTypeException("Unsupported grant type: " + tokenRequest.getGrantType());
    }

    return ok()
        .cacheControl(noStore())
        .header(PRAGMA, "no-cache")
        .body(tokenDto);
  }

  @PostMapping(path = EXTERNAL_AUTH_BASE_PATH + "/code")
  ResponseEntity<OAuth2AccessToken> authorizeAccessCode(@AuthenticationPrincipal FacebookAuthentication principal,
      @RequestParam Map<String, String> parameters) {
    log.info("Authorizing user ['{}'] access code", principal.getName());

    String clientId = getClientId(parameters);
    ClientDetails authenticatedClient = clientDetails.loadClientByClientId(clientId);

    if (!clientId.equals(authenticatedClient.getClientId())) {
      throw new InvalidClientException("Given client ID does not match authenticated client");
    }
    TokenRequest tokenRequest = externalUserAuthRequestFactory().createFacebookUserAuthRequest(parameters, principal);

    requestValidator.validateScope(tokenRequest, authenticatedClient);

    if (!StringUtils.hasText(tokenRequest.getGrantType())) {
      throw new InvalidRequestException("Missing grant type");
    }

    OAuth2AccessToken tokenDto = tokenGranter.grant(tokenRequest.getGrantType(), tokenRequest);

    if (tokenDto == null) {
      throw new UnsupportedGrantTypeException("Unsupported grant type: " + tokenRequest.getGrantType());
    }

    return ok()
        .cacheControl(noStore())
        .header(PRAGMA, "no-cache")
        .body(tokenDto);
  }

  private String getClientId(Map<String, String> authorizationParameters) {
    String clientId = authorizationParameters.get(CLIENT_ID);
    if (isBlank(clientId)) {
      throw new InvalidClientException("Missing client authentication Id");
    }

    return clientId;
  }

  private ExternalUserAuthenticationRequestFactory externalUserAuthRequestFactory() {
    return new ExternalUserAuthenticationRequestFactory(clientDetails);
  }
}
