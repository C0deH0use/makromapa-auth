package pl.code.house.makro.mapa.auth.domain.user;

import static java.time.LocalDateTime.now;
import static java.time.temporal.ChronoUnit.HOURS;
import static pl.code.house.makro.mapa.auth.domain.user.UserType.ADMIN_USER;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.Clock;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowCallbackHandler;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pl.code.house.makro.mapa.auth.error.AuthorityExistsException;

@Slf4j
@Service
public class UserAuthoritiesService {

  public static final String ROLE_PREFIX = "ROLE_";
  public static final String GET_AUTHORITY_SQL = "SELECT user_id, authority, expiry_date FROM user_authority "
      + "WHERE (expiry_date IS NULL OR expiry_date > now()) AND user_id::text = ?";
  private static final String INSERT_AUTHORITY_SQL = "INSERT INTO user_authority (user_id, authority) values (?,?)";
  private static final String INSERT_EXPIRABLE_AUTHORITY_SQL = "INSERT INTO user_authority (user_id, authority, expiry_date) values (?,?,?)";
  private static final String DELETE_AUTHORITY_SQL = "DELETE FROM user_authority WHERE user_id::text = ?";
  private static final String DELETE_EXPIRED_AUTHORITY_SQL = "DELETE FROM user_authority WHERE expiry_date < now() AND user_id::text = ?";

  private final Clock clock;
  private final JdbcTemplate jdbcTemplate;

  public UserAuthoritiesService(Clock clock, JdbcTemplate jdbcTemplate) {
    this.clock = clock;
    this.jdbcTemplate = jdbcTemplate;
  }

  public static List<GrantedAuthority> userStartAuthoritiesFor(UserType type) {
    List<GrantedAuthority> authorities = new ArrayList<>();
    authorities.add(new SimpleGrantedAuthority(ROLE_PREFIX + "FREE_USER"));
    if (ADMIN_USER == type) {
      authorities.add(new SimpleGrantedAuthority(ROLE_PREFIX + "ADMIN_USER"));
    }
    return authorities;
  }

  @Transactional
  public void insertUserAuthorities(UUID userId, UserType type) {
    log.debug("Inserting new user type: {} for user: {}", type, userId);
    deleteUserAuthorities(userId);

    for (GrantedAuthority auth : userStartAuthoritiesFor(type)) {
      jdbcTemplate.update(INSERT_AUTHORITY_SQL, userId, auth.getAuthority());
    }
  }

  @Transactional
  public void insertExpirableAuthority(UUID userId, PremiumFeature premiumFeature, Integer expiresInWeeks) {
    if (userAlreadyHasActiveFeature(userId, premiumFeature)) {
      log.error("User already has authority of type `{}` that is still active", premiumFeature);
      throw new AuthorityExistsException(userId, premiumFeature);
    }

    LocalDateTime expiryDate = calculateExpiryDateFor(expiresInWeeks);
    jdbcTemplate.update(DELETE_EXPIRED_AUTHORITY_SQL, userId.toString());
    if (expiryDate == null) {
      log.debug("Inserting new user authority type: {} that will never expire for user: {}",
          premiumFeature, userId);

      jdbcTemplate.update(INSERT_AUTHORITY_SQL, userId, ROLE_PREFIX + premiumFeature);
      return;
    }

    log.debug("Inserting new user authority type: {} that will expire at: `{}` for user: {}",
        premiumFeature, expiryDate, userId);

    jdbcTemplate.update(INSERT_EXPIRABLE_AUTHORITY_SQL, userId, ROLE_PREFIX + premiumFeature, Timestamp.valueOf(expiryDate));
  }

  private boolean userAlreadyHasActiveFeature(UUID userId, PremiumFeature premiumFeature) {
    return getUserAuthorities(userId)
        .stream()
        .map(PremiumFeature::fromAuthority)
        .anyMatch(premiumFeature::equals);
  }

  private LocalDateTime calculateExpiryDateFor(Integer expiresInWeeks) {
    if (expiresInWeeks == 0) {
      return null;
    }

    return now(clock)
        .plusWeeks(expiresInWeeks).plusHours(1)
        .truncatedTo(HOURS);
  }

  public void deleteUserAuthorities(UUID userId) {
    jdbcTemplate.update(DELETE_AUTHORITY_SQL, userId.toString());
  }

  public List<GrantedAuthority> getUserAuthorities(UUID userId) {
    AuthoritiesCallbackHandler callbackHandler = new AuthoritiesCallbackHandler(clock);
    jdbcTemplate.query(GET_AUTHORITY_SQL, callbackHandler, userId.toString());
    return callbackHandler.authorities;
  }

  @RequiredArgsConstructor
  static class AuthoritiesCallbackHandler implements RowCallbackHandler {

    private final Clock clock;
    private final List<GrantedAuthority> authorities = new ArrayList<>();

    @Override
    public void processRow(ResultSet rs) throws SQLException {
      String roleName = rs.getString(2);
      Timestamp expiryDate = rs.getTimestamp(3);

      if (expiryDate == null) {
        authorities.add(new SimpleGrantedAuthority(roleName));
      }

      if (expiryDate != null && now(clock).isBefore(expiryDate.toLocalDateTime())) {
        authorities.add(new SimpleGrantedAuthority(roleName));
      }
    }
  }
}
