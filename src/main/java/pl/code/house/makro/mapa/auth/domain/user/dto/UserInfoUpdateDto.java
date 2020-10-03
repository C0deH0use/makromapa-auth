package pl.code.house.makro.mapa.auth.domain.user.dto;

import java.io.Serializable;
import lombok.Value;

@Value
public class UserInfoUpdateDto implements Serializable {

  private static final long serialVersionUID = -2055694038701358253L;

  String name;
  String surname;
  String nickname;
  String picture;

  public UserInfoUpdateDto(
      String name,
      String surname,
      String nickname,
      String picture) {
    this.name = name;
    this.surname = surname;
    this.nickname = nickname;
    this.picture = picture;
  }
}
