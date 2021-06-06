package pl.code.house.makro.mapa.auth.configuration;

import static org.springframework.security.oauth2.jwt.NimbusJwtDecoder.withJwkSetUri;

import java.util.List;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.security.oauth2.core.DelegatingOAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2TokenValidator;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtIssuerValidator;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;

@Configuration
public class TestWebAuthorizationConfig {

  @Bean
  @Profile({"integrationTest"})
  public JwtDecoder googleJwkDecoder(
      ExternalProviders externalProviders,
      @Value("${spring.security.google.oauth2.resourceserver.jwt.issuer-uri}") String issuer,
      @Value("${spring.security.google.oauth2.resourceserver.jwt.jwk-set-uri}") String jwkSetUri
  ) {
    List<OAuth2TokenValidator<Jwt>> validators = List.of(
        new JwtIssuerValidator(issuer),
        new TokenSupplierValidator(externalProviders.clients())
    );

    NimbusJwtDecoder newDecoder = withJwkSetUri(jwkSetUri).build();
    newDecoder.setJwtValidator(new DelegatingOAuth2TokenValidator<>(validators));
    return newDecoder;
  }

  @Bean
  @Profile({"integrationTest"})
  public JwtDecoder appleIdJwkDecoder(
      ExternalProviders externalProviders,
      @Value("${spring.security.apple.oauth2.resourceserver.jwt.issuer-uri}") String issuer,
      @Value("${spring.security.apple.oauth2.resourceserver.jwt.jwk-set-uri}") String jwkSetUri
  ) {
    List<OAuth2TokenValidator<Jwt>> validators = List.of(
        new JwtIssuerValidator(issuer),
        new TokenSupplierValidator(externalProviders.clients())
    );

    NimbusJwtDecoder newDecoder = withJwkSetUri(jwkSetUri).build();
    newDecoder.setJwtValidator(new DelegatingOAuth2TokenValidator<>(validators));
    return newDecoder;
  }
}