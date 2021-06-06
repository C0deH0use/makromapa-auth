package pl.code.house.makro.mapa.auth.configuration;

import static org.springframework.security.oauth2.server.resource.BearerTokenErrors.invalidToken;

import com.nimbusds.jwt.JWTParser;
import io.vavr.control.Try;
import javax.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationManagerResolver;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.server.resource.InvalidBearerTokenException;
import org.springframework.security.oauth2.server.resource.web.BearerTokenResolver;
import org.springframework.security.oauth2.server.resource.web.DefaultBearerTokenResolver;

@RequiredArgsConstructor
public class ExternalAuthenticationManagerResolver implements AuthenticationManagerResolver<HttpServletRequest> {

  private final AuthenticationManagerResolver<HttpServletRequest> jwtResolver;
  private final AuthenticationManager opaqueAuthenticationManager;
  private final BearerTokenResolver resolver = new DefaultBearerTokenResolver();

  @Override
  public AuthenticationManager resolve(HttpServletRequest context) {
    return Try.of(() -> resolver.resolve(context))
        .andThen(this::convert)
        .map(issuer -> jwtResolver.resolve(context))
        .recover(InvalidBearerTokenException.class, opaqueAuthenticationManager)
        .getOrElseThrow(exc -> new InvalidBearerTokenException("Unknown Token", exc));
  }

  private String convert(String token) {
    if (token == null) {
      throw new OAuth2AuthenticationException(invalidToken("Token value cannot be recognized"));
    }
    try {
      String issuer = JWTParser.parse(token).getJWTClaimsSet().getIssuer();
      if (issuer != null) {
        return issuer;
      }
    } catch (Exception e) {
      throw new InvalidBearerTokenException(e.getMessage(), e);
    }
    throw new InvalidBearerTokenException("Missing issuer");
  }
}
