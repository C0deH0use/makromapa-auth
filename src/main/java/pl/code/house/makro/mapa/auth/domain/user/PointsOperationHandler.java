package pl.code.house.makro.mapa.auth.domain.user;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import pl.code.house.makro.mapa.auth.error.IllegalOperationForSelectedProductException;

@Slf4j
@RequiredArgsConstructor
abstract class PointsOperationHandler {

  private static final String PRODUCT_NOT_ACCEPTING_REASON_MESSAGE =
      "Product `%s` does not accept following operation reason to assign points to user:%s";

  private final PointProductRepository productRepository;

  private final PointsActionLogRepository logRepository;

  private final UserRepository userRepository;

  abstract boolean isAcceptable(PointsOperationDto dto);

  abstract void handle(PointsOperationDto dto);

  protected PointsProduct findAndValidateProductCorrectUsage(PointsOperationDto dto) {
    PointsProduct product = productRepository.findById(dto.getProduct())
        .orElseThrow(() -> new IllegalArgumentException("Could not find product to because of which points where earned"));
    log.info("Adding {} points to user: {}, as result of {}", product.getPoints(), dto.getUserId(), product.getName());

    if (!product.getReasonsCollection().contains(dto.getOperation())) {
      throw new IllegalOperationForSelectedProductException(String.format(PRODUCT_NOT_ACCEPTING_REASON_MESSAGE, product.getName(), dto.getOperation()));
    }
    return product;
  }

  protected void updateUserPoints(PointsOperationDto dto) {
    PointsProduct product = productRepository.findById(dto.getProduct())
        .orElseThrow(() -> new IllegalArgumentException("Could not find product to because of which points where earned"));

    userRepository.updateUserPoints(dto.getUserId(), product.getPoints());

    PointsActionLog actionLog = PointsActionLog.builder()
        .operationReason(dto.getOperation())
        .product(product)
        .points(product.getPoints())
        .userId(dto.getUserId())
        .build();
    logRepository.save(actionLog);
  }
}
