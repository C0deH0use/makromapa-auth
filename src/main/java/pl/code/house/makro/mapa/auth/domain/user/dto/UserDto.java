package pl.code.house.makro.mapa.auth.domain.user.dto;

import java.util.UUID;
import lombok.Value;
import pl.code.house.makro.mapa.auth.domain.user.OAuth2Provider;

@Value
public class UserDto {

  UUID id;

  String externalId;

  OAuth2Provider provider;

  UserDetailsDto userDetails;

  Boolean enabled;
}
