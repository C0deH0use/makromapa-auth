package pl.code.house.makro.mapa.auth.domain.user;

import static javax.persistence.AccessType.FIELD;
import static javax.persistence.EnumType.STRING;
import static lombok.AccessLevel.PACKAGE;
import static lombok.AccessLevel.PROTECTED;
import static pl.code.house.makro.mapa.auth.domain.user.UserVerificationCode.TABLE_NAME;

import java.time.ZonedDateTime;
import java.util.UUID;
import javax.persistence.Access;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Enumerated;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import pl.code.house.makro.mapa.auth.domain.AuditAwareEntity;
import pl.code.house.makro.mapa.auth.domain.user.dto.VerificationCodeDto;

@Entity
@Table(name = TABLE_NAME)
@Access(FIELD)
@Getter(PACKAGE)
@Builder(access = PACKAGE)
@EqualsAndHashCode(callSuper = false)
@NoArgsConstructor(access = PROTECTED)
@AllArgsConstructor(access = PACKAGE)
class UserVerificationCode extends AuditAwareEntity {

  static final String TABLE_NAME = "user_verification_code";

  @Id
  private UUID id;

  @OneToOne
  @JoinColumn(name = "user_id", updatable = false, nullable = false)
  private BaseUser user;

  @Column(name = "enabled", nullable = false)
  private Boolean enabled;

  @Column(name = "code", updatable = false, nullable = false)
  private String code;

  @Enumerated(STRING)
  @Column(name = "code_type", updatable = false, nullable = false)
  private CodeType codeType;

  @Column(name = "expires_on", updatable = false, nullable = false)
  private ZonedDateTime expiresOn;

  VerificationCodeDto toDto() {
    return VerificationCodeDto.builder()
        .id(id)
        .code(code)
        .user(user.toDto())
        .enabled(enabled)
        .codeType(codeType)
        .expiresOn(expiresOn)
        .build();
  }
}
