package pl.code.house.makro.mapa.auth.error.handler;

import static java.lang.String.format;

import java.util.UUID;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.Value;

@Value
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
class ErrorMessage {

  private UUID uniqueErrorId = UUID.randomUUID();

  private String error;

  static ErrorMessage from(final Exception ex) {
    return new ErrorMessage(ex.getMessage());
  }

  @Override
  public String toString() {
    return format("uniqueErrorId: '%s' | exceptionMessage: '%s'", uniqueErrorId, error);
  }
}
