package pl.code.house.makro.mapa.auth.configuration;

import static java.util.UUID.randomUUID;
import static java.util.stream.Collectors.toList;
import static org.apache.commons.lang3.StringUtils.split;
import static org.springframework.security.oauth2.jwt.NimbusJwtDecoder.withJwkSetUri;
import static pl.code.house.makro.mapa.auth.ApiConstraints.BASE_PATH;
import static pl.code.house.makro.mapa.auth.ApiConstraints.EXTERNAL_AUTH_BASE_PATH;

import java.util.List;
import java.util.Map;
import javax.sql.DataSource;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;
import org.springframework.core.annotation.Order;
import org.springframework.security.authentication.AnonymousAuthenticationProvider;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.core.DelegatingOAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2TokenValidator;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtIssuerValidator;
import org.springframework.security.oauth2.jwt.JwtTimestampValidator;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.provider.ClientDetailsService;
import org.springframework.security.oauth2.provider.approval.TokenStoreUserApprovalHandler;
import org.springframework.security.oauth2.provider.client.ClientDetailsUserDetailsService;
import org.springframework.security.oauth2.provider.client.JdbcClientDetailsService;
import org.springframework.security.oauth2.provider.request.DefaultOAuth2RequestFactory;
import org.springframework.security.oauth2.provider.token.DefaultTokenServices;
import org.springframework.security.oauth2.provider.token.ResourceServerTokenServices;
import org.springframework.security.oauth2.provider.token.TokenStore;
import org.springframework.security.oauth2.provider.token.store.JdbcTokenStore;
import org.springframework.security.oauth2.server.resource.authentication.JwtIssuerAuthenticationManagerResolver;
import org.springframework.security.provisioning.JdbcUserDetailsManager;
import pl.code.house.makro.mapa.auth.domain.token.ExternalUserAuthenticationKeyGenerator;

@Configuration
@NoArgsConstructor
@EnableWebSecurity
@EnableGlobalMethodSecurity(prePostEnabled = true)
class WebAuthorizationConfig extends WebSecurityConfigurerAdapter {

  public static List<String> trimClientIds(String androidClientId) {
    return List.of(split(androidClientId, ",")).stream().map(StringUtils::trimToEmpty).collect(toList());
  }

  private static JwtDecoder buildDecoder(String androidClientId, String issuer, String jwkSetUri) {
    List<OAuth2TokenValidator<Jwt>> validators = List.of(
        new JwtTimestampValidator(),
        new JwtIssuerValidator(issuer),
        new TokenSupplierValidator(trimClientIds(androidClientId))
    );
    NimbusJwtDecoder decoder = withJwkSetUri(jwkSetUri).build();
    decoder.setJwtValidator(new DelegatingOAuth2TokenValidator<>(validators));
    return decoder;
  }

  @Bean
  @Primary
  public TokenStore tokenStore(DataSource dataSource) {
    JdbcTokenStore jdbcTokenStore = new JdbcTokenStore(dataSource);
    jdbcTokenStore.setAuthenticationKeyGenerator(new ExternalUserAuthenticationKeyGenerator());
    return jdbcTokenStore;
  }

  @Bean
  public PasswordEncoder passwordEncoder() {
    return PasswordEncoderFactories.createDelegatingPasswordEncoder();
  }

  @Bean
  OAuth2ManagerResolver oauth2ManagerResolver(
      ResourceServerTokenServices customResourceTokenService,
      JwtIssuerAuthenticationManagerResolver multiJwtAuthenticationManagerResolver,
      FacebookAccessCodeAuthenticationProvider facebookAccessCodeAuthenticationProvider) {
    AuthenticationManager opaqueAuthenticationManager = new ProviderManager(
        new AnonymousAuthenticationProvider(randomUUID().toString()),
        new OpaqueInternalTokenAuthenticationProvider(customResourceTokenService),
        facebookAccessCodeAuthenticationProvider
    );
    return new OAuth2ManagerResolver(multiJwtAuthenticationManagerResolver, opaqueAuthenticationManager);
  }

  @Bean
  JwtIssuerAuthenticationManagerResolver multiJwtAuthenticationManagerResolver(
      JwtDecoder googleJwkDecoder,
      JwtDecoder appleIdJwkDecoder,
      @Value("${spring.security.google.oauth2.resourceserver.jwt.issuer-uri}") String googleIssuer,
      @Value("${spring.security.apple.oauth2.resourceserver.jwt.issuer-uri}") String appleIdIssuer) {
    Map<String, JwtDecoder> jwtDecoders = Map.of(
        googleIssuer, googleJwkDecoder,
        appleIdIssuer, appleIdJwkDecoder
    );
    return new JwtIssuerAuthenticationManagerResolver(new CustomIssuerJwtAuthenticationManagerResolver(jwtDecoders));
  }

  @Bean
  FacebookAccessCodeAuthenticationProvider facebookAccessCodeAuthenticationProvider(
      @Value("${spring.security.facebook.opaque.app-id}") String appId,
      @Value("${spring.security.facebook.opaque.app-namespace}") String appNamespace) {
    return new FacebookAccessCodeAuthenticationProvider(appId, appNamespace);
  }


  @Bean
  @Profile({"!integrationTest"})
  JwtDecoder googleJwkDecoder(
      @Value("${android.oauth2.client.client-id}") String androidClientId,
      @Value("${spring.security.google.oauth2.resourceserver.jwt.issuer-uri}") String issuer,
      @Value("${spring.security.google.oauth2.resourceserver.jwt.jwk-set-uri}") String jwkSetUri
  ) {
    return buildDecoder(androidClientId, issuer, jwkSetUri);
  }

  @Bean
  @Profile({"!integrationTest"})
  JwtDecoder appleIdJwkDecoder(
      @Value("${android.oauth2.client.client-id}") String androidClientId,
      @Value("${spring.security.apple.oauth2.resourceserver.jwt.issuer-uri}") String issuer,
      @Value("${spring.security.apple.oauth2.resourceserver.jwt.jwk-set-uri}") String jwkSetUri
  ) {
    return buildDecoder(androidClientId, issuer, jwkSetUri);
  }

  @Order(2)
  @Configuration
  @RequiredArgsConstructor
  public static class Oauth2AuthenticationConfig extends WebSecurityConfigurerAdapter {

    private final DataSource dataSource;

    @Override
    protected void configure(HttpSecurity http) throws Exception {
      http.csrf()
          .disable()

          .authorizeRequests()

          .anyRequest().authenticated()
      ;
    }

    @Override
    protected void configure(AuthenticationManagerBuilder auth) throws Exception {
      auth.jdbcAuthentication()
          .dataSource(dataSource)
          .usersByUsernameQuery("SELECT id, password, enabled FROM app_user WHERE provider = 'BASIC_AUTH' AND email = ?")
          .authoritiesByUsernameQuery("SELECT user_id::text, authority FROM user_authority WHERE user_id::text = ?")
      ;
    }

    @Bean
    public ResourceServerTokenServices customResourceTokenService(TokenStore tokenStore, ClientDetailsService clientDetailsService) {
      DefaultTokenServices tokenServices = new DefaultTokenServices();
      tokenServices.setTokenStore(tokenStore);
      tokenServices.setSupportRefreshToken(true);
      tokenServices.setReuseRefreshToken(true);
      tokenServices.setClientDetailsService(clientDetailsService);
      return tokenServices;
    }

    @Bean
    @Override
    public AuthenticationManager authenticationManagerBean() throws Exception {
      return super.authenticationManagerBean();
    }

    @Bean
    public UserDetailsService userDetailsServiceBean(DataSource dataSource) {
      return new JdbcUserDetailsManager(dataSource);
    }

    @Bean
    public TokenStoreUserApprovalHandler userApprovalHandler(TokenStore tokenStore, ClientDetailsService clientDetailsService) {
      TokenStoreUserApprovalHandler handler = new TokenStoreUserApprovalHandler();
      handler.setTokenStore(tokenStore);
      handler.setRequestFactory(new DefaultOAuth2RequestFactory(clientDetailsService));
      handler.setClientDetailsService(clientDetailsService);
      return handler;
    }
  }

  @Order(1)
  @Configuration
  @RequiredArgsConstructor
  static class JwtAuthenticationConfig extends WebSecurityConfigurerAdapter {

    private final DataSource dataSource;

    private final OAuth2ManagerResolver oauth2ManagerResolver;

    @Override
    public void configure(HttpSecurity http) throws Exception {

      http
          .csrf().disable()

          .oauth2ResourceServer()
          .authenticationManagerResolver(oauth2ManagerResolver)

          .and()
          .authorizeRequests().antMatchers(EXTERNAL_AUTH_BASE_PATH + "/token/**").authenticated()

          .and()
          .authorizeRequests().antMatchers(BASE_PATH + "/user-registration").authenticated()

          .and()
          .authorizeRequests().antMatchers(BASE_PATH + "/user-info").authenticated()

          .and()
          .httpBasic()

          .and()
          .userDetailsService(new ClientDetailsUserDetailsService(new JdbcClientDetailsService(dataSource)))
      ;
    }
  }
}
