package pl.code.house.makro.mapa.auth.domain.user.dto;

import lombok.Value;

@Value
public class NewUserRequest {

  String grantType;
  String clientId;
  String username;
  String password;

  public NewUserRequest(
      String grant_type,
      String client_id,
      String username,
      String password
  ) {
    this.grantType = grant_type;
    this.clientId = client_id;
    this.username = username;
    this.password = password;
  }
}
