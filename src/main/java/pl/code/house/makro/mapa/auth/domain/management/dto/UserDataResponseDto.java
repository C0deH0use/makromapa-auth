package pl.code.house.makro.mapa.auth.domain.management.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Value;

@Value
public class UserDataResponseDto {

  String url;
  @JsonProperty("confirmation_code")
  String confirmationCode;
}
