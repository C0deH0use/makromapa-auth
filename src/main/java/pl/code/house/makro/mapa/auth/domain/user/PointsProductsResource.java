package pl.code.house.makro.mapa.auth.domain.user;

import static org.springframework.http.MediaType.ALL_VALUE;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static org.springframework.http.ResponseEntity.ok;
import static pl.code.house.makro.mapa.auth.ApiConstraints.BASE_PATH;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import pl.code.house.makro.mapa.auth.domain.user.dto.PointsProductDto;

@Slf4j
@Validated
@RestController
@AllArgsConstructor
@RequestMapping(path = BASE_PATH + "/points", produces = APPLICATION_JSON_VALUE, consumes = ALL_VALUE)
class PointsProductsResource {

  private final UserPointsFacade facade;

  @GetMapping
  ResponseEntity<List<PointsProductDto>> getPointsProducts(@AuthenticationPrincipal Authentication principal) {
    log.info("User: {} requested all points products that are currently available for MakroMapa", principal.getName());

    return ok(facade.fetchAllPointsProducts());
  }
}
