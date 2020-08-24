package pl.code.house.makro.mapa.auth.domain.token;

import static pl.code.house.makro.mapa.auth.domain.user.UserAuthoritiesService.userAuthoritiesFor;

import lombok.EqualsAndHashCode;
import lombok.Value;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.social.facebook.api.User;
import pl.code.house.makro.mapa.auth.domain.user.UserType;

@Value
@EqualsAndHashCode(callSuper = true)
public class FacebookAuthentication extends AbstractAuthenticationToken {

  private static final long serialVersionUID = 1191701135435410597L;

  User userProfile;

  public FacebookAuthentication(User userProfile) {
    super(userAuthoritiesFor(UserType.FREE_USER));
    this.userProfile = userProfile;
    this.setAuthenticated(true);
  }

  public FacebookAuthentication(User userProfile, UserType userType) {
    super(userAuthoritiesFor(userType));
    this.userProfile = userProfile;
    this.setAuthenticated(true);
  }

  @Override
  public Object getCredentials() {
    return null;
  }

  @Override
  public Object getDetails() {
    return userProfile;
  }

  @Override
  public Object getPrincipal() {
    return userProfile;
  }

  @Override
  public String getName() {
    return userProfile.getEmail();
  }
}
