package pl.code.house.makro.mapa.auth.error.handler;

import static java.util.stream.Collectors.toList;
import static org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.http.ResponseEntity.badRequest;
import static org.springframework.http.ResponseEntity.status;

import com.fasterxml.jackson.databind.exc.InvalidFormatException;
import java.util.Arrays;
import java.util.Map;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.common.exceptions.ClientAuthenticationException;
import org.springframework.security.oauth2.common.exceptions.InvalidClientException;
import org.springframework.security.oauth2.common.exceptions.InvalidGrantException;
import org.springframework.security.oauth2.common.exceptions.InvalidRequestException;
import org.springframework.security.oauth2.common.exceptions.InvalidTokenException;
import org.springframework.security.oauth2.common.exceptions.UnsupportedGrantTypeException;
import org.springframework.validation.BindException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.client.RestClientException;
import org.springframework.web.context.request.WebRequest;
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
        .orElse(INTERNAL_SERVER_ERROR);

    return status(httpStatus).contentType(APPLICATION_JSON).body(errorMessage);
  }

  @ExceptionHandler({RestClientException.class})
  public ResponseEntity<Object> respondWithError(Exception ex, HandlerMethod handlerMethod) {
    final ErrorMessage errorMessage = ErrorMessage.from(ex);
    logError(ex, errorMessage.getUniqueErrorId(), handlerMethod);

    HttpStatus httpStatus = statusResolver.statusOf(ex)
        .orElse(INTERNAL_SERVER_ERROR);

    return status(httpStatus).contentType(APPLICATION_JSON).body(errorMessage);
  }

  @ExceptionHandler({IllegalArgumentException.class, IllegalStateException.class, InvalidFormatException.class,
      UnsupportedAuthenticationIssuerException.class, UnsupportedGrantTypeException.class})
  public ResponseEntity<Object> handleBadRequestException(Exception ex, HandlerMethod handlerMethod) {
    final ErrorMessage errorMessage = ErrorMessage.from(ex);

    logError(ex, errorMessage.getUniqueErrorId(), handlerMethod);
    return badRequest().contentType(APPLICATION_JSON).body(errorMessage);
  }

  @ExceptionHandler({InvalidRequestException.class, InvalidGrantException.class, InvalidTokenException.class, InvalidClientException.class})
  public ResponseEntity<Object> handleClientAuthenticationException(ClientAuthenticationException ex, HandlerMethod handlerMethod) {
    final ErrorMessage errorMessage = ErrorMessage.from(ex);

    logError(ex, errorMessage.getUniqueErrorId(), handlerMethod);
    return status(ex.getHttpErrorCode()).contentType(APPLICATION_JSON).body(errorMessage);
  }

  @Override
  protected ResponseEntity<Object> handleBindException(BindException ex, HttpHeaders headers, HttpStatus status, WebRequest request) {
    final ErrorMessage errorMessage = ErrorMessage.from(ex);
    logError(ex, errorMessage.getUniqueErrorId());

    //Get all errors
    Map<String, Object> body = Map.of("errors", ex.getBindingResult()
        .getFieldErrors()
        .stream()
        .map(error -> String.format("field: `%s`, error reason: %s", error.getField(), error.getDefaultMessage()))
        .collect(toList()));

    return badRequest().contentType(APPLICATION_JSON).body(body);
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
