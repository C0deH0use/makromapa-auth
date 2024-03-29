package pl.code.house.makro.mapa.auth.domain.token;

import static pl.code.house.makro.mapa.auth.domain.user.UserAuthoritiesService.userStartAuthoritiesFor;
import static pl.code.house.makro.mapa.auth.domain.user.UserType.FREE_USER;

import java.io.Serial;
import java.util.List;
import lombok.EqualsAndHashCode;
import lombok.Value;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.social.facebook.api.User;

@Value
@EqualsAndHashCode(callSuper = true)
public class FacebookAuthentication extends AbstractAuthenticationToken {

  @Serial
  private static final long serialVersionUID = 1191701135435410597L;

  String userName;
  User userProfile;

  public FacebookAuthentication(User userProfile) {
    super(userStartAuthoritiesFor(FREE_USER));
    this.userName = "";
    this.userProfile = userProfile;
    this.setAuthenticated(true);
  }

  public FacebookAuthentication(String sub, User userProfile, List<GrantedAuthority> userAuthorities) {
    super(userAuthorities);
    this.userName = sub;
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
