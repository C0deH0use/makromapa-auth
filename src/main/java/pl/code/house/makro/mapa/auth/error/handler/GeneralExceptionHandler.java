package pl.code.house.makro.mapa.auth.error.handler;

import static org.springframework.http.MediaType.APPLICATION_JSON;

import com.fasterxml.jackson.databind.exc.InvalidFormatException;
import java.util.Arrays;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.common.exceptions.ClientAuthenticationException;
import org.springframework.security.oauth2.common.exceptions.InvalidClientException;
import org.springframework.security.oauth2.common.exceptions.InvalidGrantException;
import org.springframework.security.oauth2.common.exceptions.InvalidRequestException;
import org.springframework.security.oauth2.common.exceptions.InvalidTokenException;
import org.springframework.security.oauth2.common.exceptions.UnsupportedGrantTypeException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.client.RestClientException;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;
import pl.code.house.makro.mapa.auth.error.UnsupportedAuthenticationIssuerException;

@Slf4j
@ControllerAdvice
public class GeneralExceptionHandler extends ResponseEntityExceptionHandler {

  private final ErrorHttpStatusResolver statusResolver = new ErrorHttpStatusResolver();

  @ExceptionHandler({Exception.class})
  public ResponseEntity<Object> unknownError(Exception ex) {
    final ErrorMessage errorMessage = ErrorMessage.from(ex);
    logError(ex, errorMessage.getUniqueErrorId());

    HttpStatus httpStatus = statusResolver.statusOf(ex)
        .orElse(HttpStatus.INTERNAL_SERVER_ERROR);

    return ResponseEntity.status(httpStatus).contentType(APPLICATION_JSON).body(errorMessage);
  }

  @ExceptionHandler({RestClientException.class})
  public ResponseEntity<Object> respondWithError(Exception ex, HandlerMethod handlerMethod) {
    final ErrorMessage errorMessage = ErrorMessage.from(ex);
    logError(ex, errorMessage.getUniqueErrorId(), handlerMethod);

    HttpStatus httpStatus = statusResolver.statusOf(ex)
        .orElse(HttpStatus.INTERNAL_SERVER_ERROR);

    return ResponseEntity.status(httpStatus).contentType(APPLICATION_JSON).body(errorMessage);
  }

  @ExceptionHandler({IllegalArgumentException.class, IllegalStateException.class, InvalidFormatException.class,
      UnsupportedAuthenticationIssuerException.class, UnsupportedGrantTypeException.class})
  public ResponseEntity<Object> handleBadRequestException(Exception ex, HandlerMethod handlerMethod) {
    final ErrorMessage errorMessage = ErrorMessage.from(ex);

    logError(ex, errorMessage.getUniqueErrorId(), handlerMethod);
    return ResponseEntity.badRequest().contentType(APPLICATION_JSON).body(errorMessage);
  }

  @ExceptionHandler({InvalidRequestException.class, InvalidGrantException.class, InvalidTokenException.class, InvalidClientException.class})
  public ResponseEntity<Object> handleClientAuthenticationException(ClientAuthenticationException ex, HandlerMethod handlerMethod) {
    final ErrorMessage errorMessage = ErrorMessage.from(ex);

    logError(ex, errorMessage.getUniqueErrorId(), handlerMethod);
    return ResponseEntity.status(ex.getHttpErrorCode()).contentType(APPLICATION_JSON).body(errorMessage);
  }

  private void logError(Exception ex, UUID errorId, HandlerMethod handlerMethod) {
    log.debug("Exception {} with message '{}' and id '{}' occurs in {} in method {}({})",
        ex.getClass().getSimpleName(),
        ex.getMessage(),
        errorId,
        handlerMethod.getBeanType().getSimpleName(),
        handlerMethod.getMethod().getName(),
        Arrays.toString(handlerMethod.getMethod().getParameters())
    );

    logError(ex, errorId);
  }

  private void logError(Exception ex, UUID errorId) {
    log.error("Exception id '{}' - {}", ex.getMessage(), errorId, ex);
  }
}
