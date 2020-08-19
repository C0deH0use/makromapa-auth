package pl.code.house.makro.mapa.auth.domain.user.dto;

import lombok.Value;

@Value
@SuppressWarnings({"PMD.FormalParameterNamingConventions", "ParameterName"})
public class NewUserRequest {

  String grantType;
  String clientId;
  String email;
  String password;

  public NewUserRequest(
      String grant_type,
      String client_id,
      String username,
      String password
  ) {
    this.grantType = grant_type;
    this.clientId = client_id;
    this.email = username;
    this.password = password;
  }
}
