package pl.code.house.makro.mapa.auth.domain.token.dto;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class IntrospectTokenDto {

  Boolean active;
  String scope;
  String username;
  String client_id;
  long exp;
}
