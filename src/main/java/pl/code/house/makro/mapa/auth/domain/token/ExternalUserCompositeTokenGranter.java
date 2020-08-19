package pl.code.house.makro.mapa.auth.domain.token;

import java.util.List;
import org.springframework.security.oauth2.provider.CompositeTokenGranter;
import org.springframework.security.oauth2.provider.TokenGranter;

class ExternalUserCompositeTokenGranter extends CompositeTokenGranter {

  ExternalUserCompositeTokenGranter(List<TokenGranter> tokenGranters) {
    super(tokenGranters);
  }
}
