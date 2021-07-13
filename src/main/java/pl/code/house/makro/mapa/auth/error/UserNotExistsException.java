package pl.code.house.makro.mapa.auth.error;

import static org.springframework.http.HttpStatus.NOT_FOUND;

import java.io.Serial;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(NOT_FOUND)
public class UserNotExistsException extends RuntimeException {

  @Serial
  private static final long serialVersionUID = -1323710575455876080L;
  private final UserOperationError error;

  public UserNotExistsException(UserOperationError error, String message) {
    super(message);
    this.error = error;
  }

  public UserOperationError getError() {
    return error;
  }
}
