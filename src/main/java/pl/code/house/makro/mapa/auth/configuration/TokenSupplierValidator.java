package pl.code.house.makro.mapa.auth.configuration;

import static java.lang.String.format;
import static org.apache.commons.lang3.StringUtils.defaultIfBlank;
import static org.springframework.security.oauth2.core.OAuth2ErrorCodes.INVALID_REQUEST;

import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.OAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2TokenValidatorResult;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.util.Assert;

@Slf4j
class TokenSupplierValidator implements OAuth2TokenValidator<Jwt> {

  private final List<String> expectedClientIds;

  TokenSupplierValidator(List<String> ids) {
    Assert.notNull(ids, "Provider clientIds cannot be null");
    Assert.isTrue(!ids.isEmpty(), "clientIds cannot be empty");
    this.expectedClientIds = ids;
  }

  @Override
  public OAuth2TokenValidatorResult validate(Jwt token) {
    Assert.notNull(token, "token cannot be null");

    String tokenClientId = defaultIfBlank(token.getClaimAsString("azp"), token.getClaimAsString("iss"));
    if (expectedClientIds.contains(tokenClientId)) {
      return OAuth2TokenValidatorResult.success();
    } else {
      log.debug("JWT token `{}` was assigned to invalid client app", tokenClientId);
      return OAuth2TokenValidatorResult.failure(invalidClientIdError(tokenClientId));
    }
  }

  private OAuth2Error invalidClientIdError(String tokenClientId) {
    return new OAuth2Error(INVALID_REQUEST,
        format("ClientId `%s` claim is not equal to the configured issuer", tokenClientId), null);
  }
}
