package pl.code.house.makro.mapa.auth.domain.product;

import static lombok.AccessLevel.PACKAGE;

import java.util.UUID;
import lombok.Builder;
import lombok.Value;
import pl.code.house.makro.mapa.auth.domain.user.dto.ProductPurchaseDto;

@Value
@Builder(access = PACKAGE)
class PointsOperationDto {

  UUID userId;
  ProductPurchaseOperation operation;
  Long product;

  static PointsOperationDto from(UUID userId, ProductPurchaseDto updatePointsDto) {
    return PointsOperationDto.builder()
        .userId(userId)
        .operation(updatePointsDto.getOperation())
        .product(updatePointsDto.getProduct())
        .build();
  }
}
