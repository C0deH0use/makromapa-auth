package pl.code.house.makro.mapa.auth.configuration;

import static java.util.stream.Collectors.toList;
import static org.apache.commons.lang3.StringUtils.split;
import static org.springframework.http.HttpStatus.UNAUTHORIZED;
import static org.springframework.security.oauth2.jose.jws.SignatureAlgorithm.RS256;

import java.util.List;
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
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.core.DelegatingOAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2TokenValidator;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtIssuerValidator;
import org.springframework.security.oauth2.jwt.JwtTimestampValidator;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.web.authentication.HttpStatusEntryPoint;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

@NoArgsConstructor
@Configuration
@EnableWebSecurity
@EnableGlobalMethodSecurity(prePostEnabled = true)
class WebAuthorizationConfig extends WebSecurityConfigurerAdapter {

  @Bean
  public PasswordEncoder passwordEncoder() {
    return new BCryptPasswordEncoder();
  }

  @Autowired
  public void configureGlobal(AuthenticationManagerBuilder auth, @Value("${admin.auth.password:PASSWORD}") String adminPassword) {
    try {
      auth.inMemoryAuthentication()
          .withUser("admin_aga")
          .password(passwordEncoder().encode(adminPassword))
          .authorities("ROLE_REGISTER");
    } catch (Exception e) {
      throw new IllegalStateException("Error when creating BASIC AUTH ADMIN", e);
    }
  }

  @Override
  public void configure(HttpSecurity http) throws Exception {
    http
        .csrf().disable()

        .requestMatcher(new AntPathRequestMatcher("/api/admin/**"))
        .httpBasic().authenticationEntryPoint(new HttpStatusEntryPoint(UNAUTHORIZED))
        .and()

        .authorizeRequests()
        .requestMatchers(EndpointRequest.to(HealthEndpoint.class, InfoEndpoint.class)).permitAll()
        .and()

        .sessionManagement().sessionCreationPolicy(SessionCreationPolicy.NEVER)
    ;
  }

  @Order(1)
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
