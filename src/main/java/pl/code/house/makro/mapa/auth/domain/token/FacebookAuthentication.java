package pl.code.house.makro.mapa.auth.domain.token;

import lombok.EqualsAndHashCode;
import lombok.Value;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.SpringSecurityCoreVersion;
import org.springframework.social.facebook.api.User;

@Value
@EqualsAndHashCode(callSuper = true)
public class FacebookAuthentication extends AbstractAuthenticationToken {

  private static final long serialVersionUID = SpringSecurityCoreVersion.SERIAL_VERSION_UID;

  User userProfile;

  public FacebookAuthentication(User userProfile) {
    super(null);
    this.userProfile = userProfile;
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
