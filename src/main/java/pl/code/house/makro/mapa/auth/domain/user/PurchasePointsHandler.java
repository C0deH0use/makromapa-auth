package pl.code.house.makro.mapa.auth.domain.user;

import static pl.code.house.makro.mapa.auth.domain.user.PointsOperationReason.PURCHASE;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
class PurchasePointsHandler extends PointsOperationHandler {

  PurchasePointsHandler(PointProductRepository productRepository, PointsActionLogRepository logRepository, UserRepository userRepository) {
    super(productRepository, logRepository, userRepository);
  }

  @Override
  boolean isAcceptable(PointsOperationDto dto) {
    return PURCHASE == dto.getOperation();
  }

  @Override
  void handle(PointsOperationDto dto) {
    PointsProduct product = findAndValidateProductCorrectUsage(dto);

    log.info("User `{}` has PURCHASE {} points by means of product: {}", dto.getUserId(), product.getPoints(), product.getName());
    updateUserPoints(dto);
  }
}