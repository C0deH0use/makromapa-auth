package pl.code.house.makro.mapa.auth.domain.receipt;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Value;

@Value
public class ValidationResponseDto {

  Integer status;

  @JsonCreator
  public ValidationResponseDto(
      @JsonProperty("status") Integer status) {
    this.status = status;
  }
}
