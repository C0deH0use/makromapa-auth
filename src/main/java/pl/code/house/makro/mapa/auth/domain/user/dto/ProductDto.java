package pl.code.house.makro.mapa.auth.domain.user.dto;

import lombok.Builder;
import lombok.Value;
import pl.code.house.makro.mapa.auth.domain.product.ProductPurchaseOperation;
import pl.code.house.makro.mapa.auth.domain.user.PremiumFeature;

@Value
@Builder
public class ProductDto {

  Long id;
  String name;
  String description;
  int points;
  boolean enabled;
  PremiumFeature premiumFeature;
  Integer expiresInWeeks;
  ProductPurchaseOperation reason;
}
