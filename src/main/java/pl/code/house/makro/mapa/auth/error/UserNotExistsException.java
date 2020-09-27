package pl.code.house.makro.mapa.auth.error;

import static org.springframework.http.HttpStatus.NOT_FOUND;

import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(NOT_FOUND)
public class UserNotExistsException extends RuntimeException {

  private static final long serialVersionUID = -1323710575455876080L;

  public UserNotExistsException(String message) {
    super(message);
  }
}
