package pl.code.house.makro.mapa.auth.domain.product;

import static java.util.stream.Collectors.joining;
import static java.util.stream.Collectors.toSet;
import static javax.persistence.AccessType.FIELD;
import static javax.persistence.GenerationType.SEQUENCE;
import static lombok.AccessLevel.PACKAGE;
import static lombok.AccessLevel.PROTECTED;

import java.util.Set;
import java.util.stream.Stream;
import javax.persistence.Access;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import pl.code.house.makro.mapa.auth.domain.AuditAwareEntity;
import pl.code.house.makro.mapa.auth.domain.user.PointsOperationReason;
import pl.code.house.makro.mapa.auth.domain.user.dto.ProductDto;

@Entity
@Table(name = Product.TABLE_NAME)
@Access(FIELD)
@Getter(PACKAGE)
@EqualsAndHashCode(callSuper = false)
@NoArgsConstructor(access = PROTECTED)
class Product extends AuditAwareEntity {

  static final String TABLE_NAME = "points_product";

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

  @Column(name = "reasons", insertable = false, updatable = false, nullable = false)
  private String reasons;

  @Builder(access = PACKAGE)
  Product(Long id, String name, String description, int points, Set<PointsOperationReason> reasons) {
    this.id = id;
    this.name = name;
    this.description = description;
    this.points = points;
    this.reasons = reasons.stream().map(Enum::name).collect(joining(DELIMITER));
  }

  Set<PointsOperationReason> getReasonsCollection() {
    return Stream.of(reasons.split(DELIMITER)).filter(StringUtils::isNoneBlank).map(PointsOperationReason::valueOf).collect(toSet());
  }

  ProductDto toDto() {
    return ProductDto.builder()
        .id(id)
        .name(name)
        .description(description)
        .points(points)
        .reasons(getReasonsCollection())
        .build();
  }
}
