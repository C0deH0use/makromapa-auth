package pl.code.house.makro.mapa.auth.error;

import static org.springframework.http.HttpStatus.BAD_REQUEST;

import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(BAD_REQUEST)
public class UnsupportedAuthenticationIssuerException extends RuntimeException {

  private static final long serialVersionUID = 2736511651591931075L;

  public UnsupportedAuthenticationIssuerException(String message) {
    super(message);
  }
}
