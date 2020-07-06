package pl.code.house.makro.mapa.auth.api;

import static lombok.AccessLevel.PRIVATE;

import lombok.NoArgsConstructor;

@NoArgsConstructor(access = PRIVATE)
public class ApiConstraints {

  public static final String GOOGLE_AUTH_BASE_PATH = "/api/auth/google";
  public static final String APPLE_AUTH_BASE_PATH = "/api/auth/apple";
}
