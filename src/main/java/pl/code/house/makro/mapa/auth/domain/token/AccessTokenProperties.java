package pl.code.house.makro.mapa.auth.domain.token;

import lombok.Value;
import org.springframework.stereotype.Component;


@Value
@Component
class AccessTokenProperties {

  private final long expiry;
  private final long refreshCodeExpiryDate;

  AccessTokenProperties(
      @org.springframework.beans.factory.annotation.Value("${user.access.token.configuration.expiry}") long expiry,
      @org.springframework.beans.factory.annotation.Value("${user.access.token.configuration.refreshCodeExpiryDate}") long refreshCodeExpiryDate) {
    this.expiry = expiry;
    this.refreshCodeExpiryDate = refreshCodeExpiryDate;
  }
}
