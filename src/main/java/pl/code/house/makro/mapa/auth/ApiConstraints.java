package pl.code.house.makro.mapa.auth;

import static lombok.AccessLevel.PRIVATE;

import lombok.NoArgsConstructor;

@NoArgsConstructor(access = PRIVATE)
public class ApiConstraints {

  public static final String BASE_PATH = "/oauth";
  public static final String EXTERNAL_AUTH_BASE_PATH =  BASE_PATH + "/external";
}
