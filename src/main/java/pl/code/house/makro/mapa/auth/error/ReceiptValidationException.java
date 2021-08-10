package pl.code.house.makro.mapa.auth.error;

import static org.springframework.http.HttpStatus.CONFLICT;

import java.io.Serial;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(CONFLICT)
public class ReceiptValidationException extends RuntimeException {

  @Serial
  private static final long serialVersionUID = -2767849033330351131L;

  public ReceiptValidationException(String message) {
    super(message);
  }
}
