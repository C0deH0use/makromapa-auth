package pl.code.house.makro.mapa.auth.error;

import static org.springframework.http.HttpStatus.PRECONDITION_FAILED;

import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(PRECONDITION_FAILED)
public class UserRegistrationException extends RuntimeException {

  private static final long serialVersionUID = -3397486044321890759L;

  private final UserOperationError error;

  public UserRegistrationException(UserOperationError error, String message) {
    super(message);
    this.error = error;
  }

  public UserOperationError getError() {
    return error;
  }
}
