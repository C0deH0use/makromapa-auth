package pl.code.house.makro.mapa.auth.domain.product;

import static java.util.UUID.fromString;
import static org.springframework.http.HttpStatus.CREATED;
import static org.springframework.http.MediaType.ALL_VALUE;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static pl.code.house.makro.mapa.auth.error.UserOperationError.USER_NOT_FOUND;

import java.util.List;
import java.util.UUID;
import javax.validation.Valid;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import pl.code.house.makro.mapa.auth.domain.user.dto.ProductDto;
import pl.code.house.makro.mapa.auth.domain.user.dto.ProductPurchaseDto;
import pl.code.house.makro.mapa.auth.domain.user.dto.UserInfoDto;
import pl.code.house.makro.mapa.auth.error.UserNotExistsException;

@Slf4j
@Validated
@RestController
@AllArgsConstructor
@RequestMapping(path = "/oauth/product", produces = APPLICATION_JSON_VALUE, consumes = ALL_VALUE)
class ProductResource {

  private final ProductFacade facade;
  private final ProductQueryFacade queryFacade;

  @GetMapping
  List<ProductDto> getProducts(Authentication principal) {
    log.info("User: {} requested all points products that are currently available for MakroMapa", principal.getName());

    return queryFacade.findAll();
  }

  @ResponseStatus(CREATED)
  @PostMapping("/{userId}")
  UserInfoDto handleUserProductPurchase(@PathVariable UUID userId, @Valid ProductPurchaseDto updatePointsDto) {
    log.info("Request to update User {} points, via {} operation:{}", userId, updatePointsDto.getOperation(), updatePointsDto.getProduct());

    return facade.handleProductBy(userId, updatePointsDto)
        .orElseThrow(() -> new UserNotExistsException(USER_NOT_FOUND, "User with following id `" + userId + " ` does not exists"));
  }

  @PostMapping
  @ResponseStatus(CREATED)
  UserInfoDto handleUserProductPurchase(Authentication principal, @Valid ProductPurchaseDto updatePointsDto) {
    UUID userId = fromString(principal.getName());
    log.info("Requesting user: {} handle product `{}` purchase/earn, via operation:{}",
        userId, updatePointsDto.getProduct(), updatePointsDto.getOperation());

    return facade.handleProductBy(userId, updatePointsDto)
        .orElseThrow(() -> new UserNotExistsException(USER_NOT_FOUND, "User with following id `" + userId + " ` does not exists"));
  }
}
