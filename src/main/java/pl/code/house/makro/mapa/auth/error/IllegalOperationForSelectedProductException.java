package pl.code.house.makro.mapa.auth.error;

import static org.springframework.http.HttpStatus.CONFLICT;

import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(CONFLICT)
public class IllegalOperationForSelectedProductException extends RuntimeException {

  private static final long serialVersionUID = -6989646217281614509L;

  public IllegalOperationForSelectedProductException(String message) {
    super(message);
  }
}
