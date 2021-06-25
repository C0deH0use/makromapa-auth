package pl.code.house.makro.mapa.auth.domain.user;

import static javax.persistence.AccessType.FIELD;
import static lombok.AccessLevel.PACKAGE;
import static lombok.AccessLevel.PROTECTED;

import java.util.Set;
import java.util.UUID;
import javax.persistence.Access;
import javax.persistence.Column;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import pl.code.house.makro.mapa.auth.domain.user.dto.UserDto;
import pl.code.house.makro.mapa.auth.domain.user.dto.UserInfoDto;

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

  @Override
  UserInfoDto toUserInfo(Set<PremiumFeature> featureSet) {
    return UserInfoDto.builder()
        .sub(this.getId())
        .name(this.getUserDetails().getName())
        .surname(this.getUserDetails().getSurname())
        .nickname(this.getUserDetails().getNickname())
        .email(this.getUserDetails().getEmail())
        .picture(this.getUserDetails().getPicture())
        .type(this.getUserDetails().getType())
        .points(this.getUserDetails().getPoints())
        .provider(this.getProvider())
        .active(this.getEnabled())
        .premiumFeatures(featureSet)
        .build();
  }
}
