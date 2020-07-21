package org.springframework.security.oauth2.provider;

import static lombok.AccessLevel.PRIVATE;
import static org.springframework.security.oauth2.common.util.OAuth2Utils.GRANT_TYPE;
import static org.springframework.security.oauth2.common.util.OAuth2Utils.RESPONSE_TYPE;
import static pl.code.house.makro.mapa.auth.domain.user.UserType.PREMIUM_USER;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Value;
import lombok.experimental.NonFinal;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import pl.code.house.makro.mapa.auth.domain.user.dto.UserDto;

@Value
@AllArgsConstructor(access = PRIVATE)
@EqualsAndHashCode(callSuper = true)
public class ExternalUserAuthRequest extends TokenRequest {

  public static final String EXTERNAL_USER_ID = "external_user_id";

  private static final long serialVersionUID = 1791623350551969647L;

  Set<String> responseTypes;

  JwtAuthenticationToken principal;

  @NonFinal
  UserDto externalUser = null;

  @NonFinal
  TokenRequest refresh = null;

  @NonFinal
  Set<String> resourceIds = new HashSet<>();

  @NonFinal
  Collection<? extends GrantedAuthority> authorities = new HashSet<>();

  public ExternalUserAuthRequest(
      Map<String, String> requestParameters,
      String clientId,
      Set<String> scopes,
      Set<String> responseTypes,
      JwtAuthenticationToken principal) {
    setClientId(clientId);
    setRequestParameters(requestParameters);
    setScope(scopes);
    this.responseTypes = responseTypes;
    this.principal = principal;
  }

  @Override
  public String getGrantType() {
    if (getRequestParameters().containsKey(GRANT_TYPE)) {
      return getRequestParameters().get(GRANT_TYPE);
    }
    if (getRequestParameters().containsKey(RESPONSE_TYPE)) {
      String response = getRequestParameters().get(RESPONSE_TYPE);
      if (response.contains("token")) {
        return "external-token";
      }
    }
    return null;
  }

  /**
   * Update the request parameters and return a new object with the same properties except the parameters.
   *
   * @param parameters new parameters replacing the existing ones
   * @return a new OAuth2Request
   */
  public ExternalUserAuthRequest createExternalUserAuthRequest(Map<String, String> parameters) {
    return new ExternalUserAuthRequest(parameters, getClientId(), getScope(), responseTypes, getPrincipal());
  }

  /**
   * Convenience method to set resourceIds and authorities on this request by inheriting from a ClientDetails object.
   */
  public void setResourceIdsAndAuthoritiesFromClientDetails(ClientDetails clientDetails) {
    setResourceIds(clientDetails.getResourceIds());
    setAuthorities(clientDetails.getAuthorities());
  }

  public void setAuthorities(Collection<? extends GrantedAuthority> authorities) {
    if (authorities != null) {
      this.authorities = new HashSet<>(authorities);
    }
  }

  public void setResourceIds(Set<String> resourceIds) {
    this.resourceIds = resourceIds;
  }

  public void setExternalUserId(UserDto externalUser) {
    this.externalUser = externalUser;
  }

  /**
   * Update the scope and create a new request. All the other properties are the same (including the request parameters).
   *
   * @param scope the new scope
   * @return a new request with the narrowed scope
   */
  public ExternalUserAuthRequest narrowScope(Set<String> scope) {
    ExternalUserAuthRequest request = new ExternalUserAuthRequest(getRequestParameters(), getClientId(), scope, responseTypes, getPrincipal());
    request.refresh = this.refresh;
    return request;
  }

  public ExternalUserAuthRequest withRefreshToken(TokenRequest tokenRequest) {
    ExternalUserAuthRequest request = new ExternalUserAuthRequest(getRequestParameters(), getClientId(), getScope(), responseTypes, getPrincipal());
    request.refresh = tokenRequest;
    return request;
  }

  /**
   * is this request is known to be for a token to be refreshed.
   *
   * @return true
   */
  public boolean isRefresh() {
    return refresh != null;
  }

  /**
   * If this request was for an access token to be refreshed, then the {@link TokenRequest} that led to the refresh
   * <i>may</i> be available here if it is known.
   *
   * @return the refresh token request (may be null)
   */
  public TokenRequest getRefreshTokenRequest() {
    return refresh;
  }

  @Override
  public OAuth2Request createOAuth2Request(ClientDetails client) {
    Map<String, String> requestParameters = getRequestParameters();
    HashMap<String, String> modifiable = new HashMap<>(requestParameters);
    // Remove password if present to prevent leaks
    modifiable.remove("password");
    modifiable.remove("client_secret");
    // Add grant type so it can be retrieved from OAuth2Request
    modifiable.put("grant_type", getGrantType());
    modifiable.put(EXTERNAL_USER_ID, externalUser.getId().toString());

    Set<String> scopes = new HashSet<>(this.getScope());
    Collection<GrantedAuthority> authorities = new HashSet<>(client.getAuthorities());
    if (PREMIUM_USER == externalUser.getUserDetails().getType()) {
      scopes.add("PREMIUM_USER");
      authorities.add(new SimpleGrantedAuthority("ROLE_PREMIUM_USER"));
    }

    return new OAuth2Request(modifiable, client.getClientId(), authorities, true, scopes,
        client.getResourceIds(), null, null, null);
  }
}
