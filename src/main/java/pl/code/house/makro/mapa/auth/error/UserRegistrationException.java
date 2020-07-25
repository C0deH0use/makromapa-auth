package pl.code.house.makro.mapa.auth.error;

import static org.springframework.http.HttpStatus.PRECONDITION_FAILED;

import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(PRECONDITION_FAILED)
public class UserRegistrationException extends RuntimeException {

  private static final long serialVersionUID = -3397486044321890759L;

  private final UserRegistrationError error;

  public UserRegistrationException(UserRegistrationError error, String message) {
    super(message);
    this.error = error;
  }

  public UserRegistrationError getError() {
    return error;
  }
}
