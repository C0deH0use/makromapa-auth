package pl.code.house.makro.mapa.auth.domain.product;

import static pl.code.house.makro.mapa.auth.domain.product.ProductPurchaseOperation.PURCHASE;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import pl.code.house.makro.mapa.auth.domain.user.UserAuthoritiesService;
import pl.code.house.makro.mapa.auth.domain.user.UserFacade;
import pl.code.house.makro.mapa.auth.domain.user.dto.ProductDto;

@Slf4j
@Service
class PurchaseProductHandler extends BaseProductHandler {

  private final UserAuthoritiesService authoritiesService;

  PurchaseProductHandler(ProductQueryFacade productFacade,
      ProductActionLogRepository logRepository, UserFacade userFacade, UserAuthoritiesService userAuthoritiesService) {
    super(productFacade, logRepository, userFacade);
    this.authoritiesService = userAuthoritiesService;
  }

  @Override
  boolean isAcceptable(PointsOperationDto dto) {
    return PURCHASE == dto.getOperation();
  }

  @Override
  void handle(PointsOperationDto dto) {
    ProductDto product = findAndValidateProductCorrectUsage(dto);

    log.info("User `{}` has PURCHASE product {}", dto.getUserId(), product.getName());
    authoritiesService.insertExpirableAuthority(dto.getUserId(), product.getPremiumFeature(), product.getExpiresInWeeks());

    updateUserPoints(dto);
  }
}