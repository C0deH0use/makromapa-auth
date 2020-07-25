package pl.code.house.makro.mapa.auth.domain.user;

import static javax.persistence.AccessType.FIELD;
import static lombok.AccessLevel.PACKAGE;
import static lombok.AccessLevel.PROTECTED;

import java.util.UUID;
import javax.persistence.Access;
import javax.persistence.Column;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import pl.code.house.makro.mapa.auth.domain.user.dto.UserDto;

@Entity
@Access(FIELD)
@Getter(PACKAGE)
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor(access = PROTECTED)
@DiscriminatorValue("ExternalUser")
class ExternalUser extends BaseUser {

  @Column(name = "external_id", updatable = false, nullable = false)
  private String externalId;

  ExternalUser(UUID id, Long termsAndConditionsId, OAuth2Provider provider, UserDetails userDetails, String externalId, Boolean enabled) {
    super(id, termsAndConditionsId, provider, userDetails, enabled);
    this.externalId = externalId;
  }

  static ExternalUser newUserFrom(OAuth2Provider authenticationProvider, UserDetails userDetails, String externalId) {
    return new ExternalUser(UUID.randomUUID(), null, authenticationProvider, userDetails, externalId, true);
  }

  @Override
  UserDto toDto() {
    return new UserDto(getId(), externalId, getProvider(), getUserDetails().toDto(), getEnabled());
  }

}
