package pl.code.house.makro.mapa.auth.domain.user;

import static javax.persistence.AccessType.FIELD;
import static lombok.AccessLevel.PACKAGE;
import static lombok.AccessLevel.PROTECTED;
import static pl.code.house.makro.mapa.auth.domain.user.OAuth2Provider.BASIC_AUTH;

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
import pl.code.house.makro.mapa.auth.domain.user.dto.UserInfoDto;

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

  UserWithPassword(UUID id, String password, Boolean enabled, Long termsAndConditionsId, UserDetails userDetails) {
    super(id, termsAndConditionsId, BASIC_AUTH, userDetails, enabled);
    this.password = password;
  }

  static UserWithPassword newDraftFrom(String encryptedPassword, UserDetails userDetails) {
    return new UserWithPassword(
        UUID.randomUUID(),
        encryptedPassword,
        false,
        null,
        userDetails);
  }

  @Override
  UserDto toDto() {
    return new UserDto(getId(), null, getProvider(), getUserDetails().toDto(), getEnabled());
  }

  @Override
  UserInfoDto toUserInfo() {
    return UserInfoDto.builder()
        .sub(this.getId())
        .name(this.getUserDetails().getName())
        .surname(this.getUserDetails().getSurname())
        .nickname(this.getUserDetails().getNickname())
        .email(this.getUserDetails().getEmail())
        .picture(this.getUserDetails().getPicture())
        .type(this.getUserDetails().getType())
        .provider(this.getProvider())
        .enabled(this.getEnabled())
        .build();
  }
}
