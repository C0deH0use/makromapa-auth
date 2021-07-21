package pl.code.house.makro.mapa.auth.error;

import static org.springframework.http.HttpStatus.PRECONDITION_REQUIRED;
import static pl.code.house.makro.mapa.auth.error.UserOperationError.NOT_ENOUGH_POINTS;

import java.io.Serial;
import lombok.Getter;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(PRECONDITION_REQUIRED)
public class NotEnoughPointsException extends RuntimeException {

  @Serial
  private static final long serialVersionUID = 4358255807862767158L;
  @Getter
  private final UserOperationError error = NOT_ENOUGH_POINTS;

  public NotEnoughPointsException(String productName, Integer pointsRequired) {
    super("User does not have enough points to use them on product: `%s` (required minimum points: %d)".formatted(productName, pointsRequired));
  }
}
