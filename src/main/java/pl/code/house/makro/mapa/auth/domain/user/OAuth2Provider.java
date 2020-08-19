package pl.code.house.makro.mapa.auth.domain.user;

import io.vavr.collection.Stream;
import pl.code.house.makro.mapa.auth.error.UnsupportedAuthenticationIssuerException;

public enum OAuth2Provider {
  GOOGLE("https://accounts.google.com"),
  FACEBOOK(""),
  APPLE("https://appleid.apple.com"),
  BASIC_AUTH("");

  private final String issuer;

  OAuth2Provider(String issuer) {
    this.issuer = issuer;
  }

  public static OAuth2Provider fromIssuer(String iss) {
    return Stream.of(values())
        .find(provider -> provider.issuer.equalsIgnoreCase(iss))
        .getOrElseThrow(() -> new UnsupportedAuthenticationIssuerException("Unknown issuer - " + iss));
  }

  public String getIssuer() {
    return issuer;
  }
}
