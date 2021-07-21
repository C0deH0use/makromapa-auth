package pl.code.house.makro.mapa.auth.error;

import static org.springframework.http.HttpStatus.PRECONDITION_FAILED;

import java.io.Serial;
import java.util.UUID;
import org.springframework.web.bind.annotation.ResponseStatus;
import pl.code.house.makro.mapa.auth.domain.user.PremiumFeature;

@ResponseStatus(PRECONDITION_FAILED)
public class AuthorityExistsException extends RuntimeException {

  private static final String EXCEPTION_MESSAGE = "User %s already contains following active feature `%s` as active!";

  @Serial
  private static final long serialVersionUID = 154510555318669220L;

  public AuthorityExistsException(UUID userId, PremiumFeature premium) {
    super(EXCEPTION_MESSAGE.formatted(userId, premium));
  }
}
