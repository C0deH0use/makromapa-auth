package pl.code.house.makro.mapa.auth.configuration;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.common.OAuth2AccessToken;
import org.springframework.security.oauth2.common.exceptions.InvalidTokenException;
import org.springframework.security.oauth2.provider.token.ResourceServerTokenServices;
import org.springframework.security.oauth2.server.resource.BearerTokenAuthenticationToken;

@Slf4j
@RequiredArgsConstructor
public class OpaqueInternalTokenAuthenticationProvider implements AuthenticationProvider {

  private final ResourceServerTokenServices resourceServerTokenServices;

  @Override
  public Authentication authenticate(Authentication authentication) {
    BearerTokenAuthenticationToken bearer = (BearerTokenAuthenticationToken) authentication;
    OAuth2AccessToken token = resourceServerTokenServices.readAccessToken(bearer.getToken());
    if (token == null) {
      log.error("Token passed for validation was not recognized as an Opaque token. Passing to next authentication provider.");
      return null;
    }

    if (token.isExpired()) {
      throw new InvalidTokenException("Token has expired");
    }

    return resourceServerTokenServices.loadAuthentication(token.getValue());
  }

  @Override
  public boolean supports(Class<?> authentication) {
    return BearerTokenAuthenticationToken.class.isAssignableFrom(authentication);
  }
}
