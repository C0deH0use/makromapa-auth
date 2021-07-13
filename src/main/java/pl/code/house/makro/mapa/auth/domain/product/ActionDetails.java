package pl.code.house.makro.mapa.auth.domain.product;

import static javax.persistence.EnumType.STRING;
import static lombok.AccessLevel.PACKAGE;
import static lombok.AccessLevel.PROTECTED;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import javax.persistence.Enumerated;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Embeddable
@ToString
@EqualsAndHashCode
@Getter(PACKAGE)
@Builder(access = PACKAGE)
@NoArgsConstructor(access = PROTECTED)
@AllArgsConstructor(access = PACKAGE)
class ActionDetails {

  @Enumerated(STRING)
  @Column(name = "reason", updatable = false, nullable = false)
  private ProductPurchaseOperation operationReason;

  @Column(name = "points", updatable = false, nullable = false)
  private int points;

  @Column(name = "product_id", nullable = false, updatable = false)
  private Long productId;
}
