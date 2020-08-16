package pl.code.house.makro.mapa.auth.error;

import static org.springframework.http.HttpStatus.PRECONDITION_FAILED;

import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(PRECONDITION_FAILED)
public class PasswordResetException extends RuntimeException {

  private static final long serialVersionUID = 3122020331182606901L;

  private final UserOperationError error;

  public PasswordResetException(UserOperationError userNotFound, String message) {
    super(message);
    this.error = userNotFound;
  }

  public UserOperationError getError() {
    return error;
  }
}
