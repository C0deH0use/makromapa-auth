package pl.code.house.makro.mapa.auth.error.handler;

import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.server.ResponseStatusException;

@Slf4j
class ErrorHttpStatusResolver {
  
  Optional<HttpStatus> statusOf(Exception ex) {
    try {
      if (ex instanceof ResponseStatusException) {
        return Optional.of(((ResponseStatusException) ex).getStatus());
      }

      ResponseStatus status = AnnotatedElementUtils.findMergedAnnotation(ex.getClass(), ResponseStatus.class);
      if (status != null) {
        return Optional.of(status.code());
      }

      if (ex.getCause() instanceof Exception) {
        return statusOf((Exception) ex.getCause());
      }
    } catch (Exception resolveEx) {
      log.warn("Handling of @ResponseStatus resulted in {}, cannot resolve http status for exception {}", resolveEx.getClass().getSimpleName(),
          ex.getClass().getName(), resolveEx);
    }
    return Optional.empty();
  }
}
