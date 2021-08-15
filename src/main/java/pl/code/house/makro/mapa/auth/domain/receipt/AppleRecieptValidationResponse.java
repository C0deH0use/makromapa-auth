package pl.code.house.makro.mapa.auth.domain.receipt;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import lombok.Value;

@Value
class AppleRecieptValidationResponse {

  Integer status;
  @JsonDeserialize(using = StoreDeserialization.class)
  StoreEnvironment environment;

  @JsonCreator
  AppleRecieptValidationResponse(
      @JsonProperty("status") Integer status,
      @JsonProperty("environment") StoreEnvironment environment
  ) {
    this.status = status;
    this.environment = environment;
  }
}
