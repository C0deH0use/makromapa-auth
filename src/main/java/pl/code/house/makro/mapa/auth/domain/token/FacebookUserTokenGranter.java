package pl.code.house.makro.mapa.auth.domain.token;

import java.util.Collection;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.common.OAuth2AccessToken;
import org.springframework.security.oauth2.common.exceptions.InvalidClientException;
import org.springframework.security.oauth2.provider.ClientDetails;
import org.springframework.security.oauth2.provider.ClientDetailsService;
import org.springframework.security.oauth2.provider.FacebookUserAuthRequest;
import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.security.oauth2.provider.OAuth2Request;
import org.springframework.security.oauth2.provider.TokenGranter;
import org.springframework.security.oauth2.provider.TokenRequest;
import org.springframework.security.oauth2.provider.token.AuthorizationServerTokenServices;
import org.springframework.security.oauth2.provider.token.TokenStore;
import pl.code.house.makro.mapa.auth.domain.user.UserAuthoritiesService;
import pl.code.house.makro.mapa.auth.domain.user.UserFacade;
import pl.code.house.makro.mapa.auth.domain.user.UserQueryFacade;
import pl.code.house.makro.mapa.auth.domain.user.dto.UserDto;

@Slf4j
@AllArgsConstructor
class FacebookUserTokenGranter implements TokenGranter {

  static final String GRANT_TYPE = "external-token";

  private final UserFacade userFacade;
  private final UserQueryFacade queryFacade;

  private final AuthorizationServerTokenServices tokenServices;

  private final TokenStore tokenStore;

  private final ClientDetailsService clientDetailsService;

  private final UserAuthoritiesService userAuthoritiesService;

  @Override
  public OAuth2AccessToken grant(String grantType, TokenRequest request) {
    if (!GRANT_TYPE.equals(grantType)) {
      return null;
    }

    if (!(request instanceof FacebookUserAuthRequest)) {
      return null;
    }

    FacebookUserAuthRequest tokenRequest = (FacebookUserAuthRequest) request;

    String clientId = tokenRequest.getClientId();
    ClientDetails client = clientDetailsService.loadClientByClientId(clientId);
    validateGrantType(grantType, client);

    log.debug("Getting access token for: " + clientId);

    return getAccessToken(client, tokenRequest);
  }

  protected OAuth2AccessToken getAccessToken(ClientDetails client, FacebookUserAuthRequest tokenRequest) {
    OAuth2Authentication auth2Authentication = getOAuth2Authentication(client, tokenRequest);
    OAuth2AccessToken existingAccessToken = tokenServices.getAccessToken(auth2Authentication);
    if (existingAccessToken != null) {
      tokenStore.removeAccessToken(existingAccessToken);
    }
    return tokenServices.createAccessToken(auth2Authentication);
  }

  private OAuth2Authentication getOAuth2Authentication(ClientDetails client, FacebookUserAuthRequest tokenRequest) {
    UserDto userDto = queryFacade.findUserByProfile(tokenRequest.getPrincipal().getUserProfile())
        .orElseGet(() -> userFacade.createUser(tokenRequest.getPrincipal().getUserProfile()));
    tokenRequest.setExternalUserId(userDto);
    List<GrantedAuthority> userAuthorities = userAuthoritiesService.getUserAuthorities(userDto.getId());
    tokenRequest.setAuthorities(userAuthorities);

    FacebookAuthentication userAuth = new FacebookAuthentication(userDto.getId().toString(), tokenRequest.getPrincipal().getUserProfile(), userAuthorities);
    OAuth2Request auth2Request = tokenRequest.createOAuth2Request(client);
    return new ExternalUserAuthentication(auth2Request, userAuth);
  }

  private void validateGrantType(String grantType, ClientDetails clientDetails) {
    Collection<String> authorizedGrantTypes = clientDetails.getAuthorizedGrantTypes();
    if (authorizedGrantTypes != null && !authorizedGrantTypes.isEmpty()
        && !authorizedGrantTypes.contains(grantType)) {
      throw new InvalidClientException("Unauthorized grant type: " + grantType);
    }
  }
}
