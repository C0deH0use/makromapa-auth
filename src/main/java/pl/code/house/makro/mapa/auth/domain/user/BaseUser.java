package pl.code.house.makro.mapa.auth.domain.user;

import static javax.persistence.AccessType.FIELD;
import static javax.persistence.EnumType.STRING;
import static javax.persistence.InheritanceType.SINGLE_TABLE;
import static lombok.AccessLevel.PACKAGE;
import static lombok.AccessLevel.PROTECTED;
import static org.apache.commons.lang3.StringUtils.defaultIfEmpty;
import static pl.code.house.makro.mapa.auth.domain.user.UserType.FREE_USER;

import java.util.UUID;
import javax.persistence.Access;
import javax.persistence.Column;
import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.Enumerated;
import javax.persistence.Id;
import javax.persistence.Inheritance;
import javax.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.DiscriminatorFormula;
import pl.code.house.makro.mapa.auth.domain.AuditAwareEntity;
import pl.code.house.makro.mapa.auth.domain.user.dto.UserDto;
import pl.code.house.makro.mapa.auth.domain.user.dto.UserInfoDto;

@Entity
@Table(name = BaseUser.TABLE_NAME)
@Access(FIELD)
@Getter(PACKAGE)
@EqualsAndHashCode(callSuper = false)
@NoArgsConstructor(access = PROTECTED)
@AllArgsConstructor(access = PACKAGE)
@DiscriminatorFormula("CASE "
    + "WHEN provider = 'BASIC_AUTH' THEN 'UserWithPassword'"
    + "WHEN provider != 'BASIC_AUTH' THEN 'ExternalUser'"
    + "END")
@Inheritance(strategy = SINGLE_TABLE)
abstract class BaseUser extends AuditAwareEntity {

  static final String TABLE_NAME = "app_user";

  @Id
  private UUID id;

  @Column(name = "terms_and_conditions_id")
  private Long termsAndConditionsId;

  @Enumerated(STRING)
  @Column(name = "provider", updatable = false, nullable = false)
  private OAuth2Provider provider;

  @Embedded
  private UserDetails userDetails;

  @Column(name = "enabled", nullable = false)
  private Boolean enabled;

  abstract UserDto toDto();

  UserType getUserType() {
    return userDetails.getType();
  }

  void activate() {
    this.enabled = true;
    this.userDetails = UserDetails.builder()
        .email(userDetails.getEmail())
        .name(userDetails.getName())
        .surname(userDetails.getSurname())
        .picture(userDetails.getPicture())
        .type(FREE_USER)
        .build();
  }

  public BaseUser updateWith(UserDetails parseUserDetails) {
    this.userDetails = UserDetails.builder()
        .email(defaultIfEmpty(parseUserDetails.getEmail(), userDetails.getEmail()))
        .name(defaultIfEmpty(parseUserDetails.getName(), userDetails.getName()))
        .surname(defaultIfEmpty(parseUserDetails.getSurname(), userDetails.getSurname()))
        .picture(defaultIfEmpty(parseUserDetails.getPicture(), userDetails.getPicture()))
        .type(userDetails.getType())
        .build();

    return this;
  }

  abstract UserInfoDto toUserInfo();
}
