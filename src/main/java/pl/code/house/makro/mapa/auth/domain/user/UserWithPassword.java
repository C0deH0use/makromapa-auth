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
import pl.code.house.makro.mapa.auth.domain.user.dto.UserDto;

@Entity
@Table(name = BaseUser.TABLE_NAME)
@Access(FIELD)
@Getter(PACKAGE)
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor(access = PROTECTED)
@DiscriminatorValue("UserWithPassword")
class UserWithPassword extends BaseUser {

  @Column(name = "password")
  private String password;

  UserWithPassword(UUID id, String password, Boolean enabled, Long termsAndConditionsId, OAuth2Provider provider, UserDetails userDetails) {
    super(id, termsAndConditionsId, provider, userDetails, enabled);
    this.password = password;
  }

  static UserWithPassword newDraftFrom(OAuth2Provider authenticationProvider, String encryptedPassword, UserDetails userDetails) {
    return new UserWithPassword(
        UUID.randomUUID(),
        encryptedPassword,
        false,
        null,
        authenticationProvider,
        userDetails);
  }

  @Override
  UserDto toDto() {
    return new UserDto(getId(), null, getProvider(), getUserDetails().toDto(), getEnabled());
  }
}
