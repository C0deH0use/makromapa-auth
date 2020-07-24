package pl.code.house.makro.mapa.auth.error;

import static org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR;

import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(INTERNAL_SERVER_ERROR)
public class EmailNotSendException extends RuntimeException {

  private static final long serialVersionUID = 1550349380553256308L;

  public EmailNotSendException(String message, Throwable cause) {
    super(message, cause);
  }
}
