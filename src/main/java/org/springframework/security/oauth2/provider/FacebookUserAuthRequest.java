package org.springframework.security.oauth2.provider;

import java.util.Map;
import java.util.Set;
import lombok.EqualsAndHashCode;
import lombok.Value;
import pl.code.house.makro.mapa.auth.domain.token.FacebookAuthentication;

@Value
@EqualsAndHashCode(callSuper = true)
public class FacebookUserAuthRequest extends AbstractUserAuthRequest {
  private static final long serialVersionUID = -7506443813950006936L;

  FacebookAuthentication principal;

  public FacebookUserAuthRequest(
      Map<String, String> requestParameters,
      String clientId,
      Set<String> scopes,
      Set<String> responseTypes,
      FacebookAuthentication principal) {
    super(requestParameters, clientId, scopes, responseTypes);
    this.principal = principal;
  }


}
