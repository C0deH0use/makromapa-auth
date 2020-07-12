package pl.code.house.makro.mapa.auth.domain.token;

import static javax.persistence.AccessType.FIELD;
import static javax.persistence.GenerationType.SEQUENCE;
import static lombok.AccessLevel.PACKAGE;
import static lombok.AccessLevel.PROTECTED;

import java.time.Clock;
import java.time.ZonedDateTime;
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
import pl.code.house.makro.mapa.auth.domain.token.dto.AccessTokenDto;
import pl.code.house.makro.mapa.auth.domain.token.dto.IntrospectTokenDto;

@Entity
@Table(name = AccessToken.TABLE_NAME)
@Access(FIELD)
@Getter(PACKAGE)
@Builder(access = PACKAGE)
@EqualsAndHashCode(callSuper = false)
@NoArgsConstructor(access = PROTECTED)
@AllArgsConstructor(access = PACKAGE)
class AccessToken extends AuditAwareEntity {

  static final String TABLE_NAME = "access_token";

  private static final int SEQ_INITIAL_VALUE = 1000;
  private static final int SEQ_INCREMENT_BY_VALUE = 1;
  private static final String SEQ_NAME = TABLE_NAME + "_seq";
  private static final String GENERATOR = TABLE_NAME + "_generator";

  @Id
  @GeneratedValue(strategy = SEQUENCE, generator = GENERATOR)
  @SequenceGenerator(name = GENERATOR, sequenceName = SEQ_NAME, allocationSize = SEQ_INCREMENT_BY_VALUE, initialValue = SEQ_INITIAL_VALUE)
  private Long id;

  @Column(name = "enabled", nullable = false)
  private boolean enabled;

  @Column(name = "user_id", updatable = false, nullable = false)
  private Long userId;

  @Column(name = "code", updatable = false, nullable = false)
  private String code;

  @Column(name = "refresh_code", updatable = false, nullable = false)
  private String refreshCode;

  @Column(name = "expiry_date", updatable = false, nullable = false)
  private ZonedDateTime expiryDate;

  @Column(name = "refresh_code_expiry_date", updatable = false, nullable = false)
  private ZonedDateTime refreshCodeExpiryDate;

  AccessTokenDto toDto() {
    return AccessTokenDto.builder()
        .userId(userId)
        .code(code)
        .refreshCode(refreshCode)
        .expiryDate(expiryDate)
        .refreshCodeExpiryDate(refreshCodeExpiryDate)
        .build();
  }

  IntrospectTokenDto toIntrospectDto(Clock clock) {
    Boolean isActive = enabled && ZonedDateTime.now(clock).isBefore(expiryDate);
    return IntrospectTokenDto.builder()
        .active(isActive)
        .client_id("123456789")
        .username(userId.toString())
        .exp(expiryDate.toEpochSecond())
        .scope("MAKROMAPA")
        .build();
  }
}