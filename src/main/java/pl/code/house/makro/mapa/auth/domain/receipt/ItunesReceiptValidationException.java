package pl.code.house.makro.mapa.auth.domain.receipt;

import static lombok.AccessLevel.PACKAGE;

import java.io.Serial;
import lombok.Getter;

class ItunesReceiptValidationException extends RuntimeException {

  @Serial
  private static final long serialVersionUID = -6684692527590395132L;

  @Getter(PACKAGE)
  private final Integer status;

  ItunesReceiptValidationException(Integer status) {
    super("Validation against Itunes failed, try debug server");
    this.status = status;
  }
}
