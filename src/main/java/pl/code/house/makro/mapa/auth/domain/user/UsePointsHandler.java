package pl.code.house.makro.mapa.auth.domain.user;

import static pl.code.house.makro.mapa.auth.domain.user.PointsOperationReason.USE;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import pl.code.house.makro.mapa.auth.domain.product.ProductFacade;
import pl.code.house.makro.mapa.auth.domain.user.dto.ProductDto;

@Slf4j
@Service
class UsePointsHandler extends PointsOperationHandler {

  UsePointsHandler(ProductFacade productFacade, PointsActionLogRepository logRepository, UserRepository userRepository) {
    super(productFacade, logRepository, userRepository);
  }

  @Override
  boolean isAcceptable(PointsOperationDto dto) {
    return USE == dto.getOperation();
  }

  @Override
  void handle(PointsOperationDto dto) {
    ProductDto product = findAndValidateProductCorrectUsage(dto);

    log.info("User `{}` has USED {} points by means of product: {}", dto.getUserId(), product.getPoints(), product.getName());
    updateUserPoints(dto);
  }
}