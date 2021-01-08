package pl.code.house.makro.mapa.auth.domain.user;

import static javax.persistence.AccessType.FIELD;
import static javax.persistence.GenerationType.SEQUENCE;
import static lombok.AccessLevel.PACKAGE;
import static lombok.AccessLevel.PROTECTED;

import javax.persistence.Access;
import javax.persistence.Column;
import javax.persistence.Entity;
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
import pl.code.house.makro.mapa.auth.domain.user.dto.TermsAndConditionsDto;

@Entity
@Table(name = TermsAndConditions.TABLE_NAME)
@Access(FIELD)
@Getter(PACKAGE)
@Builder(access = PACKAGE)
@EqualsAndHashCode(callSuper = false)
@NoArgsConstructor(access = PROTECTED)
@AllArgsConstructor(access = PACKAGE)
class TermsAndConditions extends AuditAwareEntity {

  static final String TABLE_NAME = "terms_and_conditions";

  private static final int SEQ_INITIAL_VALUE = 1000;
  private static final int SEQ_INCREMENT_BY_VALUE = 1;
  private static final String SEQ_NAME = TABLE_NAME + "_seq";
  private static final String GENERATOR = TABLE_NAME + "_generator";

  @Id
  @GeneratedValue(strategy = SEQUENCE, generator = GENERATOR)
  @SequenceGenerator(name = GENERATOR, sequenceName = SEQ_NAME, allocationSize = SEQ_INCREMENT_BY_VALUE, initialValue = SEQ_INITIAL_VALUE)
  private Long id;

  @Column(name = "contract_pl", insertable = false, updatable = false, nullable = false)
  private String contractPl;

  @Column(name = "contract_en", insertable = false, updatable = false, nullable = false)
  private String contractEn;

  TermsAndConditionsDto toDto() {
    return TermsAndConditionsDto.builder()
        .id(id)
        .contractEn(contractEn)
        .contractPl(contractPl)
        .build();
  }
}
