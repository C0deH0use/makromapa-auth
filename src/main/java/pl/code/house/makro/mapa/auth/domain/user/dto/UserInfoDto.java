package pl.code.house.makro.mapa.auth.domain.user.dto;

import java.io.Serializable;
import java.util.Set;
import java.util.UUID;
import lombok.Builder;
import lombok.Value;
import pl.code.house.makro.mapa.auth.domain.user.OAuth2Provider;
import pl.code.house.makro.mapa.auth.domain.user.PremiumFeature;
import pl.code.house.makro.mapa.auth.domain.user.UserType;

@Value
@Builder
public class UserInfoDto implements Serializable {

  private static final long serialVersionUID = 5045615582527580736L;

  UUID sub;

  OAuth2Provider provider;

  String name;

  String surname;

  String nickname;

  String email;

  String picture;

  UserType type;

  Boolean enabled;

  int points;

  Set<PremiumFeature> premiumFeatures;

}
