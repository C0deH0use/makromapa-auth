package pl.code.house.makro.mapa.auth.domain.token;

import java.util.Collection;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.common.OAuth2AccessToken;
import org.springframework.security.oauth2.common.exceptions.InvalidClientException;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.provider.ClientDetails;
import org.springframework.security.oauth2.provider.ClientDetailsService;
import org.springframework.security.oauth2.provider.ExternalUserAuthRequest;
import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.security.oauth2.provider.OAuth2Request;
import org.springframework.security.oauth2.provider.TokenGranter;
import org.springframework.security.oauth2.provider.TokenRequest;
import org.springframework.security.oauth2.provider.token.AuthorizationServerTokenServices;
import pl.code.house.makro.mapa.auth.domain.user.UserFacade;
import pl.code.house.makro.mapa.auth.domain.user.dto.UserDto;

@Slf4j
@AllArgsConstructor
class ExternalUserTokenGranter implements TokenGranter {

  static final String GRANT_TYPE = "external-token";

  private final UserFacade userFacade;

  private final AuthorizationServerTokenServices tokenServices;

  private final ClientDetailsService clientDetailsService;

  @Override
  public OAuth2AccessToken grant(String grantType, TokenRequest request) {
    if (!GRANT_TYPE.equals(grantType)) {
      return null;
    }

    if (!(request instanceof ExternalUserAuthRequest)) {
      return null;
    }

    ExternalUserAuthRequest tokenRequest = (ExternalUserAuthRequest) request;

    String clientId = tokenRequest.getClientId();
    ClientDetails client = clientDetailsService.loadClientByClientId(clientId);
    validateGrantType(grantType, client);

    log.debug("Getting access token for: " + clientId);

    return getAccessToken(client, tokenRequest);
  }

  private OAuth2Authentication getOAuth2Authentication(ClientDetails client, ExternalUserAuthRequest tokenRequest) {
    Authentication userAuth = tokenRequest.getPrincipal();
    Jwt token = tokenRequest.getPrincipal().getToken();

    UserDto userDto = userFacade.findUserByToken(token);
    tokenRequest.setExternalUserId(userDto);

    OAuth2Request auth2Request = tokenRequest.createOAuth2Request(client);
    return new ExternalUserAuthentication(auth2Request, userAuth);
  }

  protected OAuth2AccessToken getAccessToken(ClientDetails client, ExternalUserAuthRequest tokenRequest) {
    return tokenServices.createAccessToken(getOAuth2Authentication(client, tokenRequest));
  }

  private void validateGrantType(String grantType, ClientDetails clientDetails) {
    Collection<String> authorizedGrantTypes = clientDetails.getAuthorizedGrantTypes();
    if (authorizedGrantTypes != null && !authorizedGrantTypes.isEmpty()
        && !authorizedGrantTypes.contains(grantType)) {
      throw new InvalidClientException("Unauthorized grant type: " + grantType);
    }
  }
}
