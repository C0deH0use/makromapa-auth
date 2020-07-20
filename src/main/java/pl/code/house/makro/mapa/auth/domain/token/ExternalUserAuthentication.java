package pl.code.house.makro.mapa.auth.domain.token;

import static org.springframework.security.oauth2.provider.ExternalUserAuthRequest.EXTERNAL_USER_ID;

import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.security.oauth2.provider.OAuth2Request;

public class ExternalUserAuthentication extends OAuth2Authentication {

  private static final long serialVersionUID = 7934818264026751314L;

  /**
   * Construct an OAuth 2 authentication. Since some grant types don't require user authentication, the user authentication may be null.
   *
   * @param storedRequest      The authorization request (must not be null).
   * @param userAuthentication The user authentication (possibly null).
   */
  public ExternalUserAuthentication(OAuth2Request storedRequest, Authentication userAuthentication) {
    super(storedRequest, userAuthentication);
  }

  @Override
  public String getName() {
    return super.getOAuth2Request().getRequestParameters().get(EXTERNAL_USER_ID);
  }
}
