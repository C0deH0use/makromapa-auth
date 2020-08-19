package pl.code.house.makro.mapa.auth.configuration;

import java.util.HashMap;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationManagerResolver;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationProvider;

@RequiredArgsConstructor
public class CustomIssuerJwtAuthenticationManagerResolver implements AuthenticationManagerResolver<String> {

  private final Map<String, JwtDecoder> jwtDecoders;
  private final Map<String, AuthenticationManager> authenticationManagers = new HashMap<>();

  @Override
  public AuthenticationManager resolve(String issuer) {
    if (jwtDecoders.containsKey(issuer)) {
      return this.authenticationManagers.computeIfAbsent(issuer, k -> {
        JwtDecoder jwtDecoder = jwtDecoders.get(issuer);
        return new JwtAuthenticationProvider(jwtDecoder)::authenticate;
      });
    }
    return null;
  }
}
