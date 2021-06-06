package pl.code.house.makro.mapa.auth.configuration;

import static java.util.UUID.randomUUID;
import static org.springframework.security.crypto.factory.PasswordEncoderFactories.createDelegatingPasswordEncoder;
import static org.springframework.security.oauth2.jwt.NimbusJwtDecoder.withJwkSetUri;
import static org.springframework.util.StringUtils.toStringArray;
import static pl.code.house.makro.mapa.auth.ApiConstraints.EXTERNAL_AUTHENTICATION_PATH;
import static pl.code.house.makro.mapa.auth.ApiConstraints.USER_MANAGEMENT_PATH;
import static pl.code.house.makro.mapa.auth.ApiConstraints.USER_OAUTH_PATH;
import static pl.code.house.makro.mapa.auth.domain.user.UserAuthoritiesService.GET_AUTHORITY_SQL;

import java.util.List;
import java.util.Map;
import javax.sql.DataSource;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.security.SecurityProperties;
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
import org.springframework.security.core.userdetails.User;
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
import pl.code.house.makro.mapa.auth.domain.token.ExternalUserAuthenticationKeyGenerator;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
@EnableGlobalMethodSecurity(prePostEnabled = true)
class WebAuthorizationConfig extends WebSecurityConfigurerAdapter {

  private static final String WILD_CARD = "/**";

  private final DataSource dataSource;
  private final SecurityProperties securityProperties;

  private static JwtDecoder buildDecoder(List<String> providerIds, String issuer, String jwkSetUri) {
    List<OAuth2TokenValidator<Jwt>> validators = List.of(
        new JwtTimestampValidator(),
        new JwtIssuerValidator(issuer),
        new TokenSupplierValidator(providerIds)
    );
    NimbusJwtDecoder decoder = withJwkSetUri(jwkSetUri).build();
    decoder.setJwtValidator(new DelegatingOAuth2TokenValidator<>(validators));
    return decoder;
  }

  @Bean
  PasswordEncoder passwordEncoder() {
    return createDelegatingPasswordEncoder();
  }

  @Bean
  @Primary
  TokenStore tokenStore() {
    JdbcTokenStore jdbcTokenStore = new JdbcTokenStore(dataSource);
    jdbcTokenStore.setAuthenticationKeyGenerator(new ExternalUserAuthenticationKeyGenerator());
    return jdbcTokenStore;
  }

  @Bean
  @Primary
  ResourceServerTokenServices customResourceTokenService(TokenStore tokenStore, ClientDetailsService clientDetailsService) {
    DefaultTokenServices tokenServices = new DefaultTokenServices();
    tokenServices.setTokenStore(tokenStore);
    tokenServices.setSupportRefreshToken(true);
    tokenServices.setReuseRefreshToken(true);
    tokenServices.setClientDetailsService(clientDetailsService);
    return tokenServices;
  }

  @Bean
  TokenStoreUserApprovalHandler userApprovalHandler(TokenStore tokenStore, ClientDetailsService clientDetailsService) {
    TokenStoreUserApprovalHandler handler = new TokenStoreUserApprovalHandler();
    handler.setTokenStore(tokenStore);
    handler.setRequestFactory(new DefaultOAuth2RequestFactory(clientDetailsService));
    handler.setClientDetailsService(clientDetailsService);
    return handler;
  }

  @Override
  protected void configure(AuthenticationManagerBuilder auth) throws Exception {
    auth
        .inMemoryAuthentication()
        .passwordEncoder(passwordEncoder())
        .withUser(User.withUsername(securityProperties.getUser().getName())
            .password(securityProperties.getUser().getPassword())
            .roles(toStringArray(securityProperties.getUser().getRoles()))
            .build()
        )
        .and()

        .jdbcAuthentication()
        .dataSource(dataSource)
        .usersByUsernameQuery("SELECT id, password, enabled FROM app_user WHERE provider = 'BASIC_AUTH' AND email = ?")
        .authoritiesByUsernameQuery(GET_AUTHORITY_SQL)
    ;
  }

  @Bean
  @Override
  public AuthenticationManager authenticationManagerBean() throws Exception {
    return super.authenticationManagerBean();
  }

  @Bean
  ExternalAuthenticationManagerResolver externalAuthenticationManagerResolver(
      @Value("${spring.security.facebook.opaque.app-id}") String faceBookAppId,
      @Value("${spring.security.facebook.opaque.app-namespace}") String facebookAppNamespace,
      JwtIssuerAuthenticationManagerResolver multiJwtAuthenticationManagerResolver) {
    AuthenticationManager opaqueAuthenticationManager = new ProviderManager(
        new AnonymousAuthenticationProvider(randomUUID().toString()),
        new FacebookAccessCodeAuthenticationProvider(faceBookAppId, facebookAppNamespace)
    );
    return new ExternalAuthenticationManagerResolver(multiJwtAuthenticationManagerResolver, opaqueAuthenticationManager);
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
  @Profile({"!integrationTest"})
  JwtDecoder googleJwkDecoder(
      ExternalProviders externalProviders,
      @Value("${spring.security.google.oauth2.resourceserver.jwt.issuer-uri}") String issuer,
      @Value("${spring.security.google.oauth2.resourceserver.jwt.jwk-set-uri}") String jwkSetUri
  ) {
    return buildDecoder(externalProviders.clients(), issuer, jwkSetUri);
  }

  @Bean
  @Profile({"!integrationTest"})
  JwtDecoder appleIdJwkDecoder(
      ExternalProviders externalProviders,
      @Value("${spring.security.apple.oauth2.resourceserver.jwt.issuer-uri}") String issuer,
      @Value("${spring.security.apple.oauth2.resourceserver.jwt.jwk-set-uri}") String jwkSetUri
  ) {
    return buildDecoder(externalProviders.clients(), issuer, jwkSetUri);
  }

  @Order(10)
  @Configuration
  @RequiredArgsConstructor
  static class Oauth2AuthenticationConfig extends WebSecurityConfigurerAdapter {

    private final ResourceServerTokenServices tokenServices;

    @Override
    protected void configure(HttpSecurity http) throws Exception {
      http
          .csrf().disable()

          .antMatcher(USER_OAUTH_PATH + WILD_CARD)
          .authorizeRequests(req -> req.antMatchers(USER_OAUTH_PATH + WILD_CARD).authenticated())
          .oauth2ResourceServer(customizer -> customizer.authenticationManagerResolver(managerResolver()))
      ;
    }

    OpaqueTokenAuthenticationManagerResolver managerResolver() {
      AuthenticationManager authenticationManager = new ProviderManager(
          new AnonymousAuthenticationProvider(randomUUID().toString()),
          new OpaqueInternalTokenAuthenticationProvider(tokenServices)
      );
      return new OpaqueTokenAuthenticationManagerResolver(authenticationManager);
    }
  }

  @Order(20)
  @Configuration
  @RequiredArgsConstructor
  static class AuthorizedClientsAuthenticationConfig extends WebSecurityConfigurerAdapter {

    private final DataSource dataSource;

    @Override
    public void configure(HttpSecurity http) throws Exception {
      http
          .csrf().disable()

          .antMatcher(USER_MANAGEMENT_PATH + WILD_CARD)
          .authorizeRequests(req -> req.antMatchers(USER_MANAGEMENT_PATH + WILD_CARD).authenticated())

          .httpBasic()
          .and()

          .userDetailsService(new ClientDetailsUserDetailsService(new JdbcClientDetailsService(dataSource)))
      ;
    }
  }

  @Order(30)
  @Configuration
  @RequiredArgsConstructor
  static class JwtAuthenticationConfig extends WebSecurityConfigurerAdapter {

    private final ExternalAuthenticationManagerResolver externalAuthenticationManagerResolver;

    @Override
    public void configure(HttpSecurity http) throws Exception {
      http
          .csrf().disable()

          .antMatcher(EXTERNAL_AUTHENTICATION_PATH + WILD_CARD)

          .oauth2ResourceServer(customizer -> customizer.authenticationManagerResolver(externalAuthenticationManagerResolver))
          .authorizeRequests(req -> req.antMatchers(EXTERNAL_AUTHENTICATION_PATH + WILD_CARD).authenticated())
      ;
    }
  }

  @Order(40)
  @Configuration
  @RequiredArgsConstructor
  static class ActuatorsAuthenticationConfig extends WebSecurityConfigurerAdapter {

    @Override
    public void configure(HttpSecurity http) throws Exception {
      http
          .csrf().disable()

          .antMatcher("/actuator/**")

          .authorizeRequests(req -> req
              .antMatchers("/actuator/health").permitAll()
              .anyRequest().hasRole("ADMIN")
          )
          .httpBasic()
      ;
    }
  }
}
