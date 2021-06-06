package pl.code.house.makro.mapa.auth.configuration;

import static org.springframework.security.oauth2.server.resource.BearerTokenErrors.invalidToken;

import io.vavr.control.Try;
import javax.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationManagerResolver;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.server.resource.web.BearerTokenResolver;
import org.springframework.security.oauth2.server.resource.web.DefaultBearerTokenResolver;

@RequiredArgsConstructor
public class OpaqueTokenAuthenticationManagerResolver implements AuthenticationManagerResolver<HttpServletRequest> {

  private final AuthenticationManager opaqueAuthenticationManager;
  private final BearerTokenResolver resolver = new DefaultBearerTokenResolver();

  @Override
  public AuthenticationManager resolve(HttpServletRequest context) {
    return Try.of(() -> resolver.resolve(context))
        .map(this::convert)
        .get();
  }

  private AuthenticationManager convert(String token) {
    if (token == null) {
      throw new OAuth2AuthenticationException(invalidToken("Token value cannot be recognized"));
    }

    return opaqueAuthenticationManager;
  }
}