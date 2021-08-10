package pl.code.house.makro.mapa.auth;

import static lombok.AccessLevel.PRIVATE;

import lombok.NoArgsConstructor;

@NoArgsConstructor(access = PRIVATE)
public class ApiConstraints {

  public static final String OAUTH_PATH = "/oauth";
  public static final String OAUTH_USER_PATH = OAUTH_PATH + "/user";
  public static final String OAUTH_PRODUCT_PATH = OAUTH_PATH + "/product";
  public static final String OAUTH_RECEIPT_PATH = OAUTH_PATH + "/receipt";
  public static final String USER_MANAGEMENT_PATH = "/user/management";
  public static final String EXTERNAL_AUTHENTICATION_PATH = "/user/external";
}
