package pl.code.house.makro.mapa.auth.domain.product;

import static pl.code.house.makro.mapa.auth.domain.user.UserType.ADMIN_USER;

import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pl.code.house.makro.mapa.auth.domain.user.UserQueryFacade;
import pl.code.house.makro.mapa.auth.domain.user.dto.ProductPurchaseDto;
import pl.code.house.makro.mapa.auth.domain.user.dto.UserInfoDto;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProductFacade {

  private final UserQueryFacade queryFacade;
  private final ProductHandlerService handlerService;

  @Transactional
  public Optional<UserInfoDto> handleProductBy(UUID userId, ProductPurchaseDto updatePointsDto) {
    if (isNotAdminUser(userId)) {
      handlerService.handleProduct(updatePointsDto, userId);
    }

    return queryFacade.findUserById(userId);
  }

  private boolean isNotAdminUser(UUID userId) {
    return queryFacade.findUserById(userId)
        .filter(user -> ADMIN_USER == user.getType())
        .isEmpty();
  }
}