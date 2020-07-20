package pl.code.house.makro.mapa.auth.domain.user.dto;

import lombok.Value;
import pl.code.house.makro.mapa.auth.domain.user.UserType;

@Value
public class UserDetailsDto {

  String name;

  String surname;

  String email;

  String picture;

  UserType type;
}
