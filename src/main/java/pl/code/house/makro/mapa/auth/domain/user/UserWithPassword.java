package pl.code.house.makro.mapa.auth.domain.user;

import static javax.persistence.AccessType.FIELD;
import static lombok.AccessLevel.PACKAGE;
import static lombok.AccessLevel.PROTECTED;

import java.util.UUID;
import javax.persistence.Access;
import javax.persistence.Column;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.Table;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = User.TABLE_NAME)
@Access(FIELD)
@Getter(PACKAGE)
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor(access = PROTECTED)
@DiscriminatorValue("NULL")
class UserWithPassword extends User {

  @Column(name = "password")
  private String password;

  @Column(name = "enabled", nullable = false)
  private Boolean enabled;

  UserWithPassword(UUID id, String password, Boolean enabled, String externalId, Long termsAndConditionsId, OAuth2Provider provider, UserDetails userDetails) {
    super(id, externalId, termsAndConditionsId, provider, userDetails);
    this.password = password;
    this.enabled = enabled;
  }

  static UserWithPassword newDraftFrom(OAuth2Provider authenticationProvider, String encryptedPassword, UserDetails userDetails) {
    return new UserWithPassword(
        UUID.randomUUID(),
        encryptedPassword,
        false,
        null,
        null,
        authenticationProvider,
        userDetails);
  }
}
