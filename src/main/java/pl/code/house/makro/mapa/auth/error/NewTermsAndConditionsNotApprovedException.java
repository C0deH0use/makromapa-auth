package pl.code.house.makro.mapa.auth.error;

import static org.springframework.http.HttpStatus.UPGRADE_REQUIRED;

import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(UPGRADE_REQUIRED)
public class NewTermsAndConditionsNotApprovedException extends RuntimeException {

  private static final long serialVersionUID = -8818604485097523429L;

  public NewTermsAndConditionsNotApprovedException(String message) {
    super(message);
  }
}
