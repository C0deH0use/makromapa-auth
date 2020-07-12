package pl.code.house.makro.mapa.auth.domain.token.dto;

import java.time.ZonedDateTime;
import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class AccessTokenDto {

  Long userId;
  String code;
  String refreshCode;
  ZonedDateTime expiryDate;
  ZonedDateTime refreshCodeExpiryDate;

}
