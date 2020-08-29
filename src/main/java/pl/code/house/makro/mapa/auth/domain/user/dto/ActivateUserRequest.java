package pl.code.house.makro.mapa.auth.domain.user.dto;

import lombok.Value;

@Value
public class ActivateUserRequest {

  String email;
  String verificationCode;

  public ActivateUserRequest(
      String email,
      String verificationCode) {
    this.email = email;
    this.verificationCode = verificationCode;
  }
}
