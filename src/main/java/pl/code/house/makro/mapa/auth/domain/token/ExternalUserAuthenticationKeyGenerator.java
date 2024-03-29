package pl.code.house.makro.mapa.auth.domain.token;

import static java.nio.charset.StandardCharsets.UTF_8;

import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.LinkedHashMap;
import java.util.Map;
import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.security.oauth2.provider.OAuth2Request;
import org.springframework.security.oauth2.provider.token.AuthenticationKeyGenerator;

public class ExternalUserAuthenticationKeyGenerator implements AuthenticationKeyGenerator {

  private static final String CLIENT_ID = "client_id";

  private static final String USERNAME = "username";

  @Override
  public String extractKey(OAuth2Authentication authentication) {
    Map<String, String> values = new LinkedHashMap<>();
    OAuth2Request authorizationRequest = authentication.getOAuth2Request();
    if (!authentication.isClientOnly()) {
      values.put(USERNAME, authentication.getUserAuthentication().getName());
    }
    values.put(CLIENT_ID, authorizationRequest.getClientId());
    return generateKey(values);
  }

  protected String generateKey(Map<String, String> values) {
    MessageDigest digest;
    try {
      digest = MessageDigest.getInstance("MD5");
      byte[] bytes = digest.digest(values.toString().getBytes(UTF_8));
      return String.format("%032x", new BigInteger(1, bytes));
    } catch (NoSuchAlgorithmException nsae) {
      throw new IllegalStateException("MD5 algorithm not available.  Fatal (should be in the JDK).", nsae);
    }
  }
}
