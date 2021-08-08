package pl.code.house.makro.mapa.auth.domain.user.dto;

import java.io.Serial;
import java.io.Serializable;
import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class ExternalUserInfoDto implements Serializable {

  @Serial
  private static final long serialVersionUID = -2055694038701358253L;

  String email;
  String name;
  String surname;
  String nickname;
  String picture;
}
