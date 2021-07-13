package pl.code.house.makro.mapa.auth.domain.product;

import static javax.persistence.AccessType.FIELD;
import static javax.persistence.EnumType.STRING;
import static javax.persistence.GenerationType.SEQUENCE;
import static lombok.AccessLevel.PACKAGE;
import static lombok.AccessLevel.PROTECTED;

import javax.persistence.Access;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import pl.code.house.makro.mapa.auth.domain.AuditAwareEntity;
import pl.code.house.makro.mapa.auth.domain.user.dto.ProductDto;

@Entity
@Table(name = Product.TABLE_NAME)
@Access(FIELD)
@Getter(PACKAGE)
@Builder(access = PACKAGE)
@EqualsAndHashCode(callSuper = false)
@NoArgsConstructor(access = PROTECTED)
@AllArgsConstructor(access = PROTECTED)
class Product extends AuditAwareEntity {

  static final String TABLE_NAME = "product";

  private static final int SEQ_INITIAL_VALUE = 1000;
  private static final int SEQ_INCREMENT_BY_VALUE = 1;
  private static final String SEQ_NAME = TABLE_NAME + "_seq";
  private static final String GENERATOR = TABLE_NAME + "_generator";
  private static final String DELIMITER = ",";

  @Id
  @GeneratedValue(strategy = SEQUENCE, generator = GENERATOR)
  @SequenceGenerator(name = GENERATOR, sequenceName = SEQ_NAME, allocationSize = SEQ_INCREMENT_BY_VALUE, initialValue = SEQ_INITIAL_VALUE)
  private Long id;

  @Column(name = "name", insertable = false, updatable = false, nullable = false)
  private String name;

  @Column(name = "description", insertable = false, updatable = false, nullable = false)
  private String description;

  @Column(name = "points", insertable = false, updatable = false, nullable = false)
  private int points;

  @Enumerated(STRING)
  @Column(name = "reason", insertable = false, updatable = false, nullable = false)
  private ProductPurchaseOperation reason;

  @Column(name = "enabled", insertable = false, updatable = false, nullable = false)
  private boolean enabled;

  ProductDto toDto() {
    return ProductDto.builder()
        .id(id)
        .name(name)
        .description(description)
        .points(points)
        .enabled(enabled)
        .reason(reason)
        .build();
  }
}
