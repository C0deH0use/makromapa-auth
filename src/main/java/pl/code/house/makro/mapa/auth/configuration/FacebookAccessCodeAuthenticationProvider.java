package pl.code.house.makro.mapa.auth.configuration;

import static org.apache.commons.lang3.StringUtils.removeStart;

import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.server.resource.BearerTokenAuthenticationToken;
import org.springframework.social.facebook.api.User;
import org.springframework.social.facebook.api.impl.FacebookTemplate;
import pl.code.house.makro.mapa.auth.domain.token.FacebookAuthentication;

@Slf4j
class FacebookAccessCodeAuthenticationProvider implements AuthenticationProvider {

  private final String appId;
  private final String appNamespace;

  FacebookAccessCodeAuthenticationProvider(
      String appId,
      String appNamespace) {
    this.appId = appId;
    this.appNamespace = appNamespace;
  }

  @Override
  public Authentication authenticate(Authentication authentication) {
    BearerTokenAuthenticationToken bearer = (BearerTokenAuthenticationToken) authentication;
    String tokenValue = removeStart(bearer.getToken(), "Bearer ");

    FacebookTemplate template = new FacebookTemplate(tokenValue, appNamespace, appId);
    User userProfile = template.fetchObject("me", User.class, "id", "email", "first_name", "last_name");

    if (userProfile == null) {
      log.error("Token passed for validation was not recognize as Facebook AccessCode.");
    }

    return new FacebookAuthentication(userProfile);
  }

  @Override
  public boolean supports(Class<?> authentication) {
    return BearerTokenAuthenticationToken.class.isAssignableFrom(authentication);
  }
}
