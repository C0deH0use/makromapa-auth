package org.springframework.security.oauth2.provider;

import java.util.Map;
import java.util.Set;
import lombok.EqualsAndHashCode;
import lombok.Value;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;

@Value
@EqualsAndHashCode(callSuper = true)
public class ExternalUserAuthRequest extends AbstractUserAuthRequest {

  private static final long serialVersionUID = 1791623350551969647L;

  JwtAuthenticationToken principal;


  public ExternalUserAuthRequest(
      Map<String, String> requestParameters,
      String clientId,
      Set<String> scopes,
      Set<String> responseTypes,
      JwtAuthenticationToken principal) {
    super(requestParameters, clientId, scopes, responseTypes);
    this.principal = principal;
  }
}
