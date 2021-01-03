package pl.code.house.makro.mapa.auth.domain.user;

import static pl.code.house.makro.mapa.auth.domain.user.UserAuthoritiesService.ROLE_PREFIX;

import io.vavr.collection.Stream;
import org.springframework.security.core.GrantedAuthority;

public enum PremiumFeature {
  NON,
  DISABLE_ADS,
  PREMIUM;

  public static PremiumFeature fromAuthority(GrantedAuthority authority) {
    return Stream.of(values())
        .find(feature -> authority.getAuthority().equalsIgnoreCase(ROLE_PREFIX + feature.name()))
        .getOrElse(NON);
  }
}
