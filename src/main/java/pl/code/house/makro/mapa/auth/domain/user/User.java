package pl.code.house.makro.mapa.auth.domain.user;

import static javax.persistence.AccessType.FIELD;
import static javax.persistence.EnumType.STRING;
import static javax.persistence.InheritanceType.SINGLE_TABLE;
import static lombok.AccessLevel.PACKAGE;
import static lombok.AccessLevel.PROTECTED;

import java.util.UUID;
import javax.persistence.Access;
import javax.persistence.Column;
import javax.persistence.DiscriminatorColumn;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.Enumerated;
import javax.persistence.Id;
import javax.persistence.Inheritance;
import javax.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import pl.code.house.makro.mapa.auth.domain.AuditAwareEntity;
import pl.code.house.makro.mapa.auth.domain.user.dto.UserDto;

@Entity
@Table(name = User.TABLE_NAME)
@Access(FIELD)
@Getter(PACKAGE)
@Builder(access = PACKAGE)
@EqualsAndHashCode(callSuper = false)
@NoArgsConstructor(access = PROTECTED)
@AllArgsConstructor(access = PACKAGE)
@DiscriminatorColumn(name = "external_id")
@DiscriminatorValue("NOT NULL")
@Inheritance(strategy = SINGLE_TABLE)
class User extends AuditAwareEntity {

  static final String TABLE_NAME = "app_user";

  @Id
  private UUID id;

  @Column(name = "external_id", insertable = false, updatable = false, nullable = false)
  private String externalId;

  @Column(name = "terms_and_conditions_id")
  private Long termsAndConditionsId;

  @Enumerated(STRING)
  @Column(name = "provider", updatable = false, nullable = false)
  private OAuth2Provider provider;

  @Embedded
  private UserDetails userDetails;

  static User newUserFrom(OAuth2Provider authenticationProvider, String externalId, UserDetails userDetails) {
    return User.builder()
        .id(UUID.randomUUID())
        .provider(authenticationProvider)
        .externalId(externalId)
        .userDetails(userDetails)
        .build();
  }

  UserDto toDto() {
    return new UserDto(id, externalId, provider, userDetails.toDto());
  }

}
