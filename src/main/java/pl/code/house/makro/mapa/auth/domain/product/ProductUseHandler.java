package pl.code.house.makro.mapa.auth.domain.product;

import static pl.code.house.makro.mapa.auth.domain.product.ProductPurchaseOperation.USE;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import pl.code.house.makro.mapa.auth.domain.user.UserAuthoritiesService;
import pl.code.house.makro.mapa.auth.domain.user.UserFacade;
import pl.code.house.makro.mapa.auth.domain.user.UserQueryFacade;
import pl.code.house.makro.mapa.auth.domain.user.dto.ProductDto;
import pl.code.house.makro.mapa.auth.domain.user.dto.UserInfoDto;
import pl.code.house.makro.mapa.auth.error.NotEnoughPointsException;

@Slf4j
@Service
class ProductUseHandler extends BaseProductHandler {

  private final UserQueryFacade userQueryFacade;
  private final UserAuthoritiesService authoritiesService;

  ProductUseHandler(ProductQueryFacade productFacade, ProductActionLogRepository logRepository,
      UserFacade userFacade, UserQueryFacade userQueryFacade, UserAuthoritiesService userAuthoritiesService) {
    super(productFacade, logRepository, userFacade);
    this.userQueryFacade = userQueryFacade;
    this.authoritiesService = userAuthoritiesService;
  }

  @Override
  boolean isAcceptable(PointsOperationDto dto) {
    return USE == dto.getOperation();
  }

  @Override
  void handle(PointsOperationDto dto) {
    ProductDto product = findAndValidateProductCorrectUsage(dto);
    Integer userPoints = userQueryFacade.findUserById(dto.getUserId())
        .map(UserInfoDto::getPoints)
        .orElse(0);

    if (userPoints < product.getPoints()) {
      log.error("User does not have enough points to use them on product: `{}` (required minimum points: {})",
          product.getName(), product.getPoints());
      throw new NotEnoughPointsException(product.getName(), product.getPoints());
    }

    log.info("User `{}` has USED {} points by means of product: {}", dto.getUserId(), product.getPoints(), product.getName());

    authoritiesService.insertExpirableAuthority(dto.getUserId(), product.getPremiumFeature(), product.getExpiresInWeeks());
    updateUserPoints(dto);
  }
}