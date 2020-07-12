package pl.code.house.makro.mapa.auth.domain.user;

import io.vavr.collection.Stream;
import pl.code.house.makro.mapa.auth.error.UnsupportedAuthenticationIssuerException;

public enum AuthProvider {
  GOOGLE("https://accounts.google.com"),
  FACEBOOK(""),
  APPLE(""),
  BASIC_AUTH("");

  private final String issuer;

  AuthProvider(String issuer) {
    this.issuer = issuer;
  }

  public static AuthProvider fromIssuer(String iss) {
    return Stream.of(values())
        .find(provider -> provider.issuer.equalsIgnoreCase(iss))
        .getOrElseThrow(() -> new UnsupportedAuthenticationIssuerException("Unknown issuer - " + iss));
  }

  public String getIssuer() {
    return issuer;
  }
}
