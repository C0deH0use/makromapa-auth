package pl.code.house.makro.mapa.auth.configuration;

import static org.springframework.security.oauth2.jose.jws.SignatureAlgorithm.RS256;
import static pl.code.house.makro.mapa.auth.configuration.WebAuthorizationConfig.trimClientIds;

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
class TestWebAuthorizationConfig {

  @Bean
  @Profile({"integrationTest"})
  JwtDecoder nimbusJwtDecoderJwkSupport(
      @Value("${android.oauth2.client.client-id}") String androidClientId,
      @Value("${spring.security.google.oauth2.resourceserver.jwt.issuer-uri}") String issuer,
      @Value("${spring.security.google.oauth2.resourceserver.jwt.jwk-set-uri}") String jwkSetUri
  ) {
    List<OAuth2TokenValidator<Jwt>> validators = List.of(
        new JwtIssuerValidator(issuer),
        new TokenSupplierValidator(trimClientIds(androidClientId))
    );

    NimbusJwtDecoder newDecoder = NimbusJwtDecoder.withJwkSetUri(jwkSetUri)
        .jwsAlgorithm(RS256)
        .build();
    newDecoder.setJwtValidator(new DelegatingOAuth2TokenValidator<>(validators));
    return newDecoder;
  }
}