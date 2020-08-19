package pl.code.house.makro.mapa.auth.domain.user.dto;

import lombok.Value;

@Value
public class NewPasswordRequest {

  String code;
  String email;
  String newPassword;

  public NewPasswordRequest(
      String code,
      String email,
      String newPassword) {
    this.code = code;
    this.email = email;
    this.newPassword = newPassword;
  }
}
