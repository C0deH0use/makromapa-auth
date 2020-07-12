package pl.code.house.makro.mapa.auth.error;

import static org.springframework.http.HttpStatus.PRECONDITION_REQUIRED;

import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(PRECONDITION_REQUIRED)
public class InsufficientUserDetailsException extends RuntimeException {

  private static final long serialVersionUID = -2693817917970305904L;

  public InsufficientUserDetailsException(String message) {
    super(message);
  }
}
