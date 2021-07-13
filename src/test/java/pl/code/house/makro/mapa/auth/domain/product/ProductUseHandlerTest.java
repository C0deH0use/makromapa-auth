package pl.code.house.makro.mapa.auth.domain.product;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.times;
import static pl.code.house.makro.mapa.auth.domain.product.ProductPurchaseOperation.USE;
import static pl.code.house.makro.mapa.auth.domain.product.PurchaseProductHandlerTest.purchaseProduct;

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
import pl.code.house.makro.mapa.auth.domain.user.UserFacade;
import pl.code.house.makro.mapa.auth.domain.user.dto.ProductDto;
import pl.code.house.makro.mapa.auth.error.IllegalOperationForSelectedProductException;

@ExtendWith(MockitoExtension.class)
class ProductUseHandlerTest {

  static final UUID userId = UUID.randomUUID();
  static final long productId = 1000;

  @Mock
  private UserFacade userFacade;

  @Mock
  private ProductQueryFacade productQueryFacade;

  @Mock
  private ProductActionLogRepository actionLogRepository;

  @Captor
  private ArgumentCaptor<ProductActionLog> logArgumentCaptor;

  @InjectMocks
  private ProductUseHandler sut;

  @Test
  @DisplayName("should correctly handle and update user points by requested amounts")
  void shouldCorrectlyHandleAndUpdateUserPointsByRequestedAmounts() {
    //given
    PointsOperationDto dto = PointsOperationDto.builder()
        .userId(userId)
        .product(productId)
        .operation(USE)
        .build();

    given(productQueryFacade.findById(productId)).willReturn(Optional.of(useOnAdsProduct()));

    //when
    sut.handle(dto);

    //then
    then(userFacade).should(times(1)).updateUserPoints(userId, 100);
    then(actionLogRepository).should(times(1)).save(logArgumentCaptor.capture());

    ProductActionLog capturedValue = logArgumentCaptor.getValue();
    assertThat(capturedValue.getUserId()).isEqualTo(userId);
    assertThat(capturedValue.getDetails().getPoints()).isEqualTo(100);
    assertThat(capturedValue.getDetails().getProductId()).isEqualTo(productId);
    assertThat(capturedValue.getDetails().getOperationReason()).isEqualTo(USE);
  }

  @Test
  @DisplayName("should correctly handle and update user when using the correct product")
  void shouldCorrectlyHandleAndUpdateUserWhenUsingTheCorrectProduct() {
    //given
    PointsOperationDto dto = PointsOperationDto.builder()
        .userId(userId)
        .product(productId)
        .operation(USE)
        .build();

    given(productQueryFacade.findById(productId)).willReturn(Optional.of(useOnPremiumProduct()));

    //when
    sut.handle(dto);

    //then
    then(userFacade).should(times(1)).updateUserPoints(userId, 300);
    then(actionLogRepository).should(times(1)).save(logArgumentCaptor.capture());

    ProductActionLog capturedValue = logArgumentCaptor.getValue();
    assertThat(capturedValue.getUserId()).isEqualTo(userId);
    assertThat(capturedValue.getDetails().getPoints()).isEqualTo(300);
    assertThat(capturedValue.getDetails().getProductId()).isEqualTo(productId);
    assertThat(capturedValue.getDetails().getOperationReason()).isEqualTo(USE);
  }

  @Test
  @DisplayName("should throw if product in request does not allow to be used with selected points operation reason")
  void shouldThrowIfProductInRequestDoesNotAllowToBeUsedWithSelectedPointsOperationReason() {
    //given
    PointsOperationDto dto = PointsOperationDto.builder()
        .userId(userId)
        .product(productId)
        .operation(USE)
        .build();

    given(productQueryFacade.findById(productId)).willReturn(Optional.of(purchaseProduct()));

    //when
    assertThatThrownBy(() -> sut.handle(dto))
        .isInstanceOf(IllegalOperationForSelectedProductException.class)
        .hasMessage("Product `Points purchased` does not accept following operation reason to assign points to user:USE");
  }

  @Test
  @DisplayName("should throw if product cannot be found")
  void shouldThrowIfProductCannotBeFound() {
    //given
    PointsOperationDto dto = PointsOperationDto.builder()
        .product(productId)
        .build();

    given(productQueryFacade.findById(productId)).willReturn(Optional.empty());

    //when
    assertThatThrownBy(() -> sut.handle(dto))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("Could not find product of which points where earned");
  }

  static ProductDto useOnAdsProduct() {
    return ProductDto.builder()
        .id(productId)
        .name("Points Used on Ads")
        .points(100)
        .reason(USE)
        .build();
  }

  static ProductDto useOnPremiumProduct() {
    return ProductDto.builder()
        .id(productId)
        .name("Points Used on PREMIUM")
        .points(300)
        .reason(USE)
        .build();
  }
}