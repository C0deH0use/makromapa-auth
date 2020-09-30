package pl.code.house.makro.mapa.auth.domain.user.dto;

import java.io.Serializable;
import lombok.Value;
import pl.code.house.makro.mapa.auth.domain.user.UserType;

@Value
public class UserDetailsDto implements Serializable {

  private static final long serialVersionUID = -7168999264463691414L;

  String name;

  String surname;

  String nickname;

  String email;

  String picture;

  UserType type;

  boolean showNickOnly;
}
