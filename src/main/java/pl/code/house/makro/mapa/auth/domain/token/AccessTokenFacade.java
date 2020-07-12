package pl.code.house.makro.mapa.auth.domain.token;

import static java.time.ZonedDateTime.now;

import java.time.Clock;
import java.util.Optional;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import pl.code.house.makro.mapa.auth.domain.token.dto.AccessTokenDto;
import pl.code.house.makro.mapa.auth.domain.token.dto.IntrospectTokenDto;

@Slf4j
@Component
@AllArgsConstructor
public class AccessTokenFacade {

  private final Clock clock;

  private final AccessTokenProperties tokenProperties;

  private final AccessTokenRepository repository;

  public Optional<AccessTokenDto> findOneById(Long tokenId) {
    return repository.findById(tokenId)
        .map(AccessToken::toDto);
  }

  public Optional<IntrospectTokenDto> introspectAccessCode(String accessCode) {
    log.debug("Searching for active token by access code `{}`", accessCode);

    return repository.findByAccessCode(accessCode)
        .map(accessToken -> accessToken.toIntrospectDto(clock));
  }

  @Transactional
  public AccessTokenDto issueTokenFor(Long userId) {
    log.info("Issuing new token for user `{}`", userId);

    AccessToken token = AccessToken.builder()
        .userId(userId)
        .enabled(true)
        .expiryDate(now(clock).plusSeconds(tokenProperties.getExpiry()))
        .refreshCodeExpiryDate(now(clock).plusSeconds(tokenProperties.getRefreshCodeExpiryDate()))
        .code(generateCode())
        .refreshCode(generateCode())
        .build();

    return repository.save(token).toDto();
  }

  @Transactional
  public int revokeAllTokensFor(Long userId) {
    log.info("Revoking token(s) associate with user `{}`", userId);

    return repository.revokeTokensForUserId(userId);
  }

  private static String generateCode() {
    return UUID.randomUUID().toString();
  }

}
