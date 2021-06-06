package pl.code.house.makro.mapa.auth;

import static lombok.AccessLevel.PRIVATE;

import lombok.NoArgsConstructor;

@NoArgsConstructor(access = PRIVATE)
public class ApiConstraints {

  public static final String USER_OAUTH_PATH = "/user/oauth";
  public static final String USER_MANAGEMENT_PATH = "/user/management";
  public static final String EXTERNAL_AUTHENTICATION_PATH = "/user/external";
}
