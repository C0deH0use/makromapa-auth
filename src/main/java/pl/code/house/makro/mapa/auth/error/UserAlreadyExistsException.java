package pl.code.house.makro.mapa.auth.error;

import static org.springframework.http.HttpStatus.BAD_REQUEST;

import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(BAD_REQUEST)
public class UserAlreadyExistsException extends RuntimeException {

  private static final long serialVersionUID = 6789128435323487486L;

  public UserAlreadyExistsException(String message) {
    super(message);
  }
}
