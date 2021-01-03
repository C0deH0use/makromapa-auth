package pl.code.house.makro.mapa.auth.domain.token;

import java.util.List;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.provider.ClientDetailsService;
import org.springframework.security.oauth2.provider.OAuth2RequestFactory;
import org.springframework.security.oauth2.provider.TokenGranter;
import org.springframework.security.oauth2.provider.refresh.RefreshTokenGranter;
import org.springframework.security.oauth2.provider.request.DefaultOAuth2RequestFactory;
import org.springframework.security.oauth2.provider.token.AuthorizationServerTokenServices;
import org.springframework.security.oauth2.provider.token.TokenStore;
import pl.code.house.makro.mapa.auth.domain.user.UserAuthoritiesService;
import pl.code.house.makro.mapa.auth.domain.user.UserFacade;

@Configuration
class ExternalUserTokenConfiguration {

  @Bean
  ExternalUserCompositeTokenGranter externalUserTokenGranter(
      UserFacade userFacade,
      @Qualifier("defaultAuthorizationServerTokenServices") AuthorizationServerTokenServices tokenService,
      TokenStore tokenStore,
      ClientDetailsService clientDetailsService,
      UserAuthoritiesService userAuthoritiesService) {
    return new ExternalUserCompositeTokenGranter(tokenGranters(userFacade, clientDetailsService, userAuthoritiesService, tokenService, tokenStore));
  }

  private List<TokenGranter> tokenGranters(UserFacade userFacade, ClientDetailsService clientDetails,
      UserAuthoritiesService userAuthoritiesService, AuthorizationServerTokenServices tokenService, TokenStore tokenStore) {
    OAuth2RequestFactory factory = new DefaultOAuth2RequestFactory(clientDetails);
    return List.of(
        new RefreshTokenGranter(tokenService, clientDetails, factory),
        new FacebookUserTokenGranter(userFacade, tokenService, tokenStore, clientDetails, userAuthoritiesService),
        new ExternalUserTokenGranter(userFacade, tokenService, tokenStore, clientDetails, userAuthoritiesService)
    );
  }
}
