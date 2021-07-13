package pl.code.house.makro.mapa.auth.domain.product;

import static pl.code.house.makro.mapa.auth.domain.product.ProductPurchaseOperation.EARN;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import pl.code.house.makro.mapa.auth.domain.user.UserFacade;
import pl.code.house.makro.mapa.auth.domain.user.dto.ProductDto;

@Slf4j
@Service
class EarnProductHandler extends BaseProductHandler {

  EarnProductHandler(ProductQueryFacade productFacade, ProductActionLogRepository logRepository, UserFacade userFacade) {
    super(productFacade, logRepository, userFacade);
  }

  @Override
  boolean isAcceptable(PointsOperationDto dto) {
    return EARN == dto.getOperation();
  }

  @Override
  void handle(PointsOperationDto dto) {
    ProductDto product = findAndValidateProductCorrectUsage(dto);

    log.info("User `{}` has EARNED {} points by means of product: {}", dto.getUserId(), product.getPoints(), product.getName());
    updateUserPoints(dto);
  }
}