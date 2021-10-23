package pl.code.house.makro.mapa.auth.configuration;

import javax.sql.DataSource;
import lombok.AllArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.oauth2.config.annotation.configurers.ClientDetailsServiceConfigurer;
import org.springframework.security.oauth2.config.annotation.web.configuration.AuthorizationServerConfigurerAdapter;
import org.springframework.security.oauth2.config.annotation.web.configuration.EnableAuthorizationServer;
import org.springframework.security.oauth2.config.annotation.web.configurers.AuthorizationServerEndpointsConfigurer;
import org.springframework.security.oauth2.config.annotation.web.configurers.AuthorizationServerSecurityConfigurer;
import org.springframework.security.oauth2.provider.approval.UserApprovalHandler;
import org.springframework.security.oauth2.provider.token.DefaultAccessTokenConverter;
import org.springframework.security.oauth2.provider.token.DefaultUserAuthenticationConverter;
import org.springframework.security.oauth2.provider.token.TokenStore;

@Configuration
@AllArgsConstructor
@EnableAuthorizationServer
public class AuthorizationServerConfiguration extends AuthorizationServerConfigurerAdapter {

  private final DataSource dataSource;

  private final TokenStore tokenStore;

  private final UserApprovalHandler userApprovalHandler;

  private final AuthenticationManager authenticationManager;

  @Override
  public void configure(ClientDetailsServiceConfigurer clients) throws Exception {
    clients.jdbc(dataSource);
  }

  @Override
  public void configure(AuthorizationServerEndpointsConfigurer endpoints) {
    endpoints
        .tokenStore(tokenStore)
        .accessTokenConverter(getAccessTokenConverter())
        .userApprovalHandler(userApprovalHandler)
        .authenticationManager(authenticationManager)
    ;
  }

  @Override
  public void configure(AuthorizationServerSecurityConfigurer oauthServer) {
    oauthServer
        .realm("makromapa/oauth2")
        .tokenKeyAccess("denyAll()")
        .checkTokenAccess("isAuthenticated() and hasAnyRole('MAKROMAPA_BACKEND', 'USER_INFO_AUTH')")
    ;
  }

  private DefaultAccessTokenConverter getAccessTokenConverter() {
    DefaultUserAuthenticationConverter userAuthenticationConverter = new DefaultUserAuthenticationConverter();
    userAuthenticationConverter.setUserClaimName("sub");
    DefaultAccessTokenConverter accessTokenConverter = new DefaultAccessTokenConverter();
    accessTokenConverter.setUserTokenConverter(userAuthenticationConverter);
    return accessTokenConverter;
  }
}
