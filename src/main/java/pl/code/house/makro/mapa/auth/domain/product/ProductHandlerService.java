package pl.code.house.makro.mapa.auth.domain.product;

import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import pl.code.house.makro.mapa.auth.domain.user.dto.ProductPurchaseDto;

@Slf4j
@Service
@RequiredArgsConstructor
class ProductHandlerService {

  private final List<BaseProductHandler> productHandlers;

  void handleProduct(ProductPurchaseDto updatePointsDto, UUID userId) {
    PointsOperationDto operationDto = PointsOperationDto.from(userId, updatePointsDto);

    productHandlers.stream()
        .filter(handler -> handler.isAcceptable(operationDto))
        .findFirst()
        .ifPresent(handler -> handler.handle(operationDto));
  }
}
