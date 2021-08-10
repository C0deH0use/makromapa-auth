package pl.code.house.makro.mapa.auth.domain.receipt;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.io.Serial;
import java.io.Serializable;
import javax.validation.constraints.NotNull;
import lombok.Value;

@Value
public class ReceiptValidationDto implements Serializable {

  @Serial
  private static final long serialVersionUID = -8026445977094778801L;

  @NotNull(message = "Missing receipt that is send for validation")
  String localReceipt;

  @NotNull(message = "Missing store in which purchase was done")
  Store store;

  @JsonCreator
  public ReceiptValidationDto(
      @JsonProperty("localReceipt") String localReceipt,
      @JsonProperty("store") Store store) {
    this.localReceipt = localReceipt;
    this.store = store;
  }
}
