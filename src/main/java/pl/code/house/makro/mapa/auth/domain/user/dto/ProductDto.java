package pl.code.house.makro.mapa.auth.domain.user.dto;

import java.util.Set;
import lombok.Builder;
import lombok.Value;
import pl.code.house.makro.mapa.auth.domain.product.ProductPurchaseOperation;

@Value
@Builder
public class ProductDto {

  Long id;
  String name;
  String description;
  int points;
  boolean enabled;
  Set<ProductPurchaseOperation> reasons;
}
