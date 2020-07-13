package pl.code.house.makro.mapa.auth.configuration;

import static java.util.stream.Collectors.toList;
import static org.apache.commons.lang3.StringUtils.split;
import static org.springframework.http.HttpStatus.UNAUTHORIZED;
import static org.springframework.security.oauth2.jose.jws.SignatureAlgorithm.RS256;

import java.util.List;
import javax.sql.DataSource;
import lombok.NoArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.actuate.autoconfigure.security.servlet.EndpointRequest;
import org.springframework.boot.actuate.health.HealthEndpoint;
import org.springframework.boot.actuate.info.InfoEndpoint;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.core.annotation.Order;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
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
import org.springframework.security.oauth2.provider.request.DefaultOAuth2RequestFactory;
import org.springframework.security.oauth2.provider.token.TokenStore;
import org.springframework.security.oauth2.provider.token.store.JdbcTokenStore;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.authentication.HttpStatusEntryPoint;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

@Configuration
@NoArgsConstructor
@EnableWebSecurity
@EnableGlobalMethodSecurity(prePostEnabled = true)
class WebAuthorizationConfig extends WebSecurityConfigurerAdapter {
  @Order(1)
  @Configuration
  public static class Oauth2AuthenticationConfig extends WebSecurityConfigurerAdapter {

    @Bean
    @Override
    public AuthenticationManager authenticationManagerBean() throws Exception {
      return super.authenticationManagerBean();
    }

    @Bean
    @Override
    public UserDetailsService userDetailsServiceBean() throws Exception {
      PasswordEncoder encoder = PasswordEncoderFactories.createDelegatingPasswordEncoder();
      return new InMemoryUserDetailsManager(
          User.withUsername("enduser").password(encoder.encode("password")).roles("USER").build());
    }

    @Bean
    public TokenStore tokenStore(DataSource dataSource) {
      return new JdbcTokenStore(dataSource);
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

  @Order(2)
  @Configuration
  public static class GoogleAuthenticationConfig extends WebSecurityConfigurerAdapter {

    @Value("${android.oauth2.client.client-id}")
    private String androidClientId;

    @Value("${spring.security.google.oauth2.resourceserver.jwt.issuer-uri}")
    private String issuer;

    @Value("${spring.security.google.oauth2.resourceserver.jwt.jwk-set-uri}")
    private String jwkSetUri;

    @Autowired
    @Qualifier("nimbusJwtDecoderJwkSupport")
    private JwtDecoder decoder;

    @Override
    protected void configure(HttpSecurity http) throws Exception {
      http
          .requestMatcher(new AntPathRequestMatcher("/api/auth/authorize"))
          .authorizeRequests().anyRequest().authenticated()

          .and()
          .oauth2ResourceServer()
          .jwt()
          .decoder(decoder);
    }

    @Override
    @SuppressWarnings("PMD.UselessOverridingMethod")
    public void configure(WebSecurity web) throws Exception {
      super.configure(web);
    }


    @Bean
    @Profile({"!integrationTest"})
    JwtDecoder nimbusJwtDecoderJwkSupport() {
      List<OAuth2TokenValidator<Jwt>> validators = List.of(
          new JwtTimestampValidator(),
          new JwtIssuerValidator(issuer),
          new TokenSupplierValidator(trimClientIds(androidClientId))
      );
      NimbusJwtDecoder decoder = NimbusJwtDecoder.withJwkSetUri(jwkSetUri)
          .jwsAlgorithm(RS256)
          .build();
      decoder.setJwtValidator(new DelegatingOAuth2TokenValidator<>(validators));
      return decoder;
    }
  }

  public static List<String> trimClientIds(String androidClientId) {
    return List.of(split(androidClientId, ",")).stream().map(StringUtils::trimToEmpty).collect(toList());
  }
}
