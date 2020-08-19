package pl.code.house.makro.mapa.auth.domain.user.dto;

import java.io.Serializable;
import java.util.UUID;
import lombok.Value;
import pl.code.house.makro.mapa.auth.domain.user.OAuth2Provider;

@Value
public class UserDto implements Serializable {

  private static final long serialVersionUID = -8256154333316080006L;

  UUID id;

  String externalId;

  OAuth2Provider provider;

  UserDetailsDto userDetails;

  Boolean enabled;
}
