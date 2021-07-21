package pl.code.house.makro.mapa.auth.domain.product;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.times;
import static pl.code.house.makro.mapa.auth.domain.product.ProductPurchaseOperation.USE;
import static pl.code.house.makro.mapa.auth.domain.product.PurchaseProductHandlerTest.purchaseProduct;
import static pl.code.house.makro.mapa.auth.domain.user.PremiumFeature.DISABLE_ADS;
import static pl.code.house.makro.mapa.auth.domain.user.PremiumFeature.PREMIUM;

import java.util.Optional;
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
import pl.code.house.makro.mapa.auth.domain.user.UserQueryFacade;
import pl.code.house.makro.mapa.auth.domain.user.dto.ProductDto;
import pl.code.house.makro.mapa.auth.domain.user.dto.UserInfoDto;
import pl.code.house.makro.mapa.auth.error.IllegalOperationForSelectedProductException;
import pl.code.house.makro.mapa.auth.error.NotEnoughPointsException;

@ExtendWith(MockitoExtension.class)
class ProductUseHandlerTest {

  static final UUID userId = UUID.randomUUID();
  static final long productId = 1000;

  @Mock
  private UserQueryFacade queryFacade;

  @Mock
  private UserFacade userFacade;

  @Mock
  private ProductQueryFacade productQueryFacade;

  @Mock
  private UserAuthoritiesService authoritiesService;

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
    given(queryFacade.findUserById(userId)).willReturn(userInfo());

    //when
    sut.handle(dto);

    //then
    then(userFacade).should(times(1)).updateUserPoints(userId, -100);
    then(authoritiesService).should(times(1)).insertExpirableAuthority(userId, DISABLE_ADS, 1);
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
    given(queryFacade.findUserById(userId)).willReturn(userInfo());

    //when
    sut.handle(dto);

    //then
    then(userFacade).should(times(1)).updateUserPoints(userId, -300);
    then(authoritiesService).should(times(1)).insertExpirableAuthority(userId, PREMIUM, 4);
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
        .hasMessage("Product `Premium product` does not accept following operation reason to assign points to user:USE");
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
        .hasMessage("Could not find product by id [1000] that user want's to purchase");
  }

  @Test
  @DisplayName("should throw if user cannot afford product")
  void shouldThrowIfUserCannotAffordProduct() {
    //given
    PointsOperationDto dto = PointsOperationDto.builder()
        .userId(userId)
        .product(productId)
        .operation(USE)
        .build();

    given(queryFacade.findUserById(userId)).willReturn(Optional.of(userWithPoints(100)));
    given(productQueryFacade.findById(productId)).willReturn(Optional.of(useOnPremiumProduct()));

    //when
    assertThatThrownBy(() -> sut.handle(dto))
        .isInstanceOf(NotEnoughPointsException.class)
        .hasMessage("User does not have enough points to use them on product: `Points Used on PREMIUM` (required minimum points: 300)");
  }

  static ProductDto useOnAdsProduct() {
    return ProductDto.builder()
        .id(productId)
        .name("Points Used on Ads")
        .points(100)
        .reason(USE)
        .premiumFeature(DISABLE_ADS)
        .expiresInWeeks(1)
        .build();
  }

  static ProductDto useOnPremiumProduct() {
    return ProductDto.builder()
        .id(productId)
        .name("Points Used on PREMIUM")
        .points(300)
        .reason(USE)
        .premiumFeature(PREMIUM)
        .expiresInWeeks(4)
        .build();
  }

  static Optional<UserInfoDto> userInfo() {
    return Optional.of(userWithPoints(1000));
  }

  static UserInfoDto userWithPoints(int points) {
    return UserInfoDto.builder()
        .points(points)
        .build();
  }
}