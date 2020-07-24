package pl.code.house.makro.mapa.auth.domain.user;

import static javax.persistence.AccessType.FIELD;
import static lombok.AccessLevel.PACKAGE;
import static lombok.AccessLevel.PROTECTED;
import static pl.code.house.makro.mapa.auth.domain.user.UserActivationCode.TABLE_NAME;

import java.time.ZonedDateTime;
import java.util.UUID;
import javax.persistence.Access;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = TABLE_NAME)
@Access(FIELD)
@Getter(PACKAGE)
@Builder(access = PACKAGE)
@EqualsAndHashCode(callSuper = false)
@NoArgsConstructor(access = PROTECTED)
@AllArgsConstructor(access = PACKAGE)
class UserActivationCode {

  static final String TABLE_NAME = "user_activation_code";

  @Id
  private UUID id;

  @OneToOne
  @JoinColumn(name = "draft_user_id", updatable = false, insertable = false, nullable = false)
  private User draftUser;

  @Column(name = "enabled", nullable = false)
  private Boolean enabled;

  @Column(name = "code", updatable = false, nullable = false)
  private String code;

  @Column(name = "expires_on", updatable = false, insertable = false, nullable = false)
  private ZonedDateTime expiresOn;

}
