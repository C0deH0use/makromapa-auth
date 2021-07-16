package pl.code.house.makro.mapa.auth.domain.product;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import pl.code.house.makro.mapa.auth.domain.user.UserFacade;
import pl.code.house.makro.mapa.auth.domain.user.dto.ProductDto;
import pl.code.house.makro.mapa.auth.error.IllegalOperationForSelectedProductException;

@Slf4j
@RequiredArgsConstructor
abstract class BaseProductHandler {

  private static final String PRODUCT_NOT_ACCEPTING_REASON_MESSAGE =
      "Product `%s` does not accept following operation reason to assign points to user:%s";
  private static final String PRODUCT_NOT_FOUND_MESSAGE = "Could not find product by id [%s] that user want's to purchase";

  private final ProductQueryFacade productQueryFacade;

  private final ProductActionLogRepository logRepository;

  private final UserFacade userFacade;

  abstract boolean isAcceptable(PointsOperationDto dto);

  abstract void handle(PointsOperationDto dto);

  protected ProductDto findAndValidateProductCorrectUsage(PointsOperationDto dto) {
    ProductDto product = productQueryFacade.findById(dto.getProduct())
        .orElseThrow(() -> new IllegalArgumentException(PRODUCT_NOT_FOUND_MESSAGE.formatted(dto.getProduct())));
    log.info("Adding {} points to user: {}, as result of {}", product.getPoints(), dto.getUserId(), product.getName());

    if (product.getReason() != dto.getOperation()) {
      throw new IllegalOperationForSelectedProductException(String.format(PRODUCT_NOT_ACCEPTING_REASON_MESSAGE, product.getName(), dto.getOperation()));
    }
    return product;
  }

  protected void updateUserPoints(PointsOperationDto dto) {
    ProductDto product = productQueryFacade.findById(dto.getProduct())
        .orElseThrow(() -> new IllegalArgumentException("Could not find product of which points where earned"));
    int pointsGranted = calculatePointsThatUserShouldBeGranted(product);

    userFacade.updateUserPoints(dto.getUserId(), pointsGranted);

    ProductActionLog actionLog = ProductActionLog.builder()
        .userId(dto.getUserId())
        .details(ActionDetails.builder()
            .operationReason(dto.getOperation())
            .productId(product.getId())
            .points(product.getPoints())
            .build()
        )
        .build();
    logRepository.save(actionLog);
  }

  private int calculatePointsThatUserShouldBeGranted(ProductDto productDto) {
    if (ProductPurchaseOperation.EARN == productDto.getReason()) {
      return productDto.getPoints();
    }

    return productDto.getPoints() * (-1);
  }
}
