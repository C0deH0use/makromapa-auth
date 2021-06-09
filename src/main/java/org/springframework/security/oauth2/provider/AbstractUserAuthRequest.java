package org.springframework.security.oauth2.provider;

import static java.util.stream.Collectors.toSet;
import static lombok.AccessLevel.PRIVATE;
import static org.apache.commons.lang3.StringUtils.removeStart;
import static org.springframework.security.oauth2.common.util.OAuth2Utils.GRANT_TYPE;
import static org.springframework.security.oauth2.common.util.OAuth2Utils.RESPONSE_TYPE;
import static pl.code.house.makro.mapa.auth.domain.user.UserAuthoritiesService.ROLE_PREFIX;
import static pl.code.house.makro.mapa.auth.domain.user.UserType.ADMIN_USER;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Value;
import lombok.experimental.NonFinal;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import pl.code.house.makro.mapa.auth.domain.user.UserType;
import pl.code.house.makro.mapa.auth.domain.user.dto.UserDto;

@Value
@NonFinal
@AllArgsConstructor(access = PRIVATE)
@EqualsAndHashCode(callSuper = true)
public abstract class AbstractUserAuthRequest extends TokenRequest {

  public static final String USER_ID = "user_id";

  private static final long serialVersionUID = 1791623350551969647L;

  Set<String> responseTypes;

  @NonFinal
  UserDto externalUser = null;

  @NonFinal
  TokenRequest refresh = null;

  @NonFinal
  Set<String> resourceIds = new HashSet<>();

  @NonFinal
  Collection<GrantedAuthority> authorities = new HashSet<>();

  public AbstractUserAuthRequest(
      Map<String, String> requestParameters,
      String clientId,
      Set<String> scopes,
      Set<String> responseTypes) {
    setClientId(clientId);
    setRequestParameters(requestParameters);
    setScope(scopes);
    this.responseTypes = responseTypes;
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

  public abstract Authentication getPrincipal();

  @Override
  public OAuth2Request createOAuth2Request(ClientDetails client) {
    Map<String, String> requestParameters = getRequestParameters();
    HashMap<String, String> modifiable = new HashMap<>(requestParameters);
    // Remove password if present to prevent leaks
    modifiable.remove("password");
    modifiable.remove("client_secret");
    // Add grant type so it can be retrieved from OAuth2Request
    modifiable.put("grant_type", getGrantType());
    modifiable.put(USER_ID, externalUser.getId().toString());

    Set<String> scopes = this.getAuthorities()
        .stream()
        .map(GrantedAuthority::getAuthority)
        .map(authority -> removeStart(authority, ROLE_PREFIX))
        .collect(toSet());
    UserType userType = externalUser.getUserDetails().getType();

    if (ADMIN_USER == userType) {
      scopes.add("ADMIN");
      authorities.add(new SimpleGrantedAuthority("ROLE_ADMIN_USER"));
    }

    return new OAuth2Request(modifiable, client.getClientId(), getAuthorities(), true, scopes,
        client.getResourceIds(), null, null, null);
  }
}
