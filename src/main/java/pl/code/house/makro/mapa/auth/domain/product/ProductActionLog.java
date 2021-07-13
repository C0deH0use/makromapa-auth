package pl.code.house.makro.mapa.auth.domain.product;

import static javax.persistence.AccessType.FIELD;
import static javax.persistence.GenerationType.AUTO;
import static lombok.AccessLevel.PACKAGE;
import static lombok.AccessLevel.PROTECTED;

import java.util.UUID;
import javax.persistence.Access;
import javax.persistence.Column;
import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import pl.code.house.makro.mapa.auth.domain.AuditAwareEntity;

@Entity
@Table(name = ProductActionLog.TABLE_NAME)
@Access(FIELD)
@Getter(PACKAGE)
@Builder(access = PACKAGE)
@EqualsAndHashCode(callSuper = false)
@NoArgsConstructor(access = PROTECTED)
@AllArgsConstructor(access = PACKAGE)
class ProductActionLog extends AuditAwareEntity {

  static final String TABLE_NAME = "product_action_log";

  private static final int SEQ_INITIAL_VALUE = 1000;
  private static final int SEQ_INCREMENT_BY_VALUE = 1;
  private static final String SEQ_NAME = TABLE_NAME + "_seq";
  private static final String GENERATOR = TABLE_NAME + "_generator";

  @Id
  @GeneratedValue(strategy = AUTO)
  private UUID id;

  @Column(name = "user_id", updatable = false, nullable = false)
  private UUID userId;

  @Embedded
  private ActionDetails details;

}
