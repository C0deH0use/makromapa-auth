package pl.code.house.makro.mapa.auth.domain.token;

import static org.springframework.security.oauth2.common.util.OAuth2Utils.CLIENT_ID;
import static org.springframework.security.oauth2.common.util.OAuth2Utils.RESPONSE_TYPE;
import static org.springframework.security.oauth2.common.util.OAuth2Utils.SCOPE;
import static org.springframework.security.oauth2.common.util.OAuth2Utils.parseParameterList;

import java.util.Map;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.springframework.security.oauth2.provider.ClientDetails;
import org.springframework.security.oauth2.provider.ClientDetailsService;
import org.springframework.security.oauth2.provider.ExternalUserAuthRequest;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;

@RequiredArgsConstructor
class ExternalUserAuthenticationRequestFactory {

  private final ClientDetailsService clientDetailsService;

  ExternalUserAuthRequest createExternalUserAuthRequest(Map<String, String> requestParameters, JwtAuthenticationToken principal) {
    String clientId = requestParameters.get(CLIENT_ID);
    Set<String> scopes = extractScopes(requestParameters, clientId);
    Set<String> responseTypes = parseParameterList(requestParameters.get(RESPONSE_TYPE));
    ClientDetails clientDetails = clientDetailsService.loadClientByClientId(clientId);

    ExternalUserAuthRequest authRequest = new ExternalUserAuthRequest(requestParameters, clientId, scopes, responseTypes, principal);
    authRequest.setResourceIdsAndAuthoritiesFromClientDetails(clientDetails);
    return authRequest;
  }

  private Set<String> extractScopes(Map<String, String> requestParameters, String clientId) {
    Set<String> scopes = parseParameterList(requestParameters.get(SCOPE));
    ClientDetails clientDetails = clientDetailsService.loadClientByClientId(clientId);
    if (scopes.isEmpty()) {
      scopes = clientDetails.getScope();
    }

    return scopes;
  }
}
