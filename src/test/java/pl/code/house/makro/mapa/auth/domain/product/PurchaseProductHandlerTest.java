package pl.code.house.makro.mapa.auth.domain.product;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.times;
import static pl.code.house.makro.mapa.auth.domain.product.ProductPurchaseOperation.EARN;
import static pl.code.house.makro.mapa.auth.domain.product.ProductPurchaseOperation.PURCHASE;
import static pl.code.house.makro.mapa.auth.domain.user.PremiumFeature.PREMIUM;

import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import pl.code.house.makro.mapa.auth.domain.user.UserAuthoritiesService;
import pl.code.house.makro.mapa.auth.domain.user.UserFacade;
import pl.code.house.makro.mapa.auth.domain.user.dto.ProductDto;
import pl.code.house.makro.mapa.auth.error.IllegalOperationForSelectedProductException;

@ExtendWith(MockitoExtension.class)
class PurchaseProductHandlerTest {

  static final UUID userId = UUID.randomUUID();
  static final long productId = 1000;

  @Mock
  private UserFacade userFacade;

  @Mock
  private ProductQueryFacade productFacade;

  @Mock
  private ProductActionLogRepository actionLogRepository;

  @Mock
  private UserAuthoritiesService authoritiesService;

  @Captor
  private ArgumentCaptor<ProductActionLog> logArgumentCaptor;

  @InjectMocks
  private PurchaseProductHandler sut;

  @Test
  @DisplayName("should correctly handle and update user points by requested amounts")
  void shouldCorrectlyHandleAndUpdateUserPointsByRequestedAmounts() {
    //given
    PointsOperationDto dto = PointsOperationDto.builder()
        .userId(userId)
        .product(productId)
        .operation(PURCHASE)
        .build();

    given(productFacade.findById(productId)).willReturn(Optional.of(purchaseProduct()));

    //when
    sut.handle(dto);

    //then
    then(userFacade).should(times(1)).updateUserPoints(userId, 0);
    then(authoritiesService).should(times(1)).insertExpirableAuthority(userId, PREMIUM, 0);
    then(actionLogRepository).should(times(1)).save(logArgumentCaptor.capture());

    ProductActionLog capturedValue = logArgumentCaptor.getValue();
    assertThat(capturedValue.getUserId()).isEqualTo(userId);
    assertThat(capturedValue.getDetails().getPoints()).isEqualTo(0);
    assertThat(capturedValue.getDetails().getProductId()).isEqualTo(productId);
    assertThat(capturedValue.getDetails().getOperationReason()).isEqualTo(PURCHASE);
  }

  @Test
  @DisplayName("should throw if product in request does not allow to be used with selected points operation reason")
  void shouldThrowIfProductInRequestDoesNotAllowToBeUsedWithSelectedPointsOperationReason() {
    //given
    PointsOperationDto dto = PointsOperationDto.builder()
        .userId(userId)
        .product(productId)
        .operation(EARN)
        .build();

    given(productFacade.findById(productId)).willReturn(Optional.of(purchaseProduct()));

    //when
    assertThatThrownBy(() -> sut.handle(dto))
        .isInstanceOf(IllegalOperationForSelectedProductException.class)
        .hasMessage("Product `Premium product` does not accept following operation reason to assign points to user:EARN");
  }

  @Test
  @DisplayName("should throw if product cannot be found")
  void shouldThrowIfProductCannotBeFound() {
    //given
    PointsOperationDto dto = PointsOperationDto.builder()
        .product(productId)
        .build();

    given(productFacade.findById(productId)).willReturn(Optional.empty());

    //when
    assertThatThrownBy(() -> sut.handle(dto))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("Could not find product by id [1000] that user want's to purchase");
  }

  static ProductDto earnProduct() {
    return ProductDto.builder()
        .id(productId)
        .name("Points earned when dish proposal was approved")
        .points(20)
        .reason(EARN)
        .build();
  }

  static ProductDto purchaseProduct() {
    return ProductDto.builder()
        .id(productId)
        .name("Premium product")
        .points(0)
        .reason(PURCHASE)
        .premiumFeature(PREMIUM)
        .expiresInWeeks(0)
        .build();
  }
}