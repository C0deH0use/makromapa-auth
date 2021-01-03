package pl.code.house.makro.mapa.auth.domain.user;

import static java.time.LocalDateTime.now;
import static pl.code.house.makro.mapa.auth.domain.user.UserType.ADMIN_USER;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowCallbackHandler;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserAuthoritiesService {

  public static final String ROLE_PREFIX = "ROLE_";
  public static final String GET_AUTHORITY_SQL = "SELECT authority, expiry_date FROM user_authority "
      + "WHERE (expiry_date IS NULL OR expiry_date > now()) AND user_id::text = ?";
  private static final String INSERT_AUTHORITY_SQL = "INSERT INTO user_authority (user_id, authority) values (?,?)";
  private static final String INSERT_EXPIRABLE_AUTHORITY_SQL = "INSERT INTO user_authority (user_id, authority, expiry_date) values (?,?,?)";
  private static final String DELETE_AUTHORITY_SQL = "DELETE FROM user_authority WHERE user_id::text = ?";
  private static final String DELETE_EXPIRED_AUTHORITY_SQL = "DELETE FROM user_authority WHERE expiry_date < now() AND user_id::text = ?";

  private final JdbcTemplate jdbcTemplate;

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
    deleteUserAuthorities(userId);

    for (GrantedAuthority auth : userStartAuthoritiesFor(type)) {
      jdbcTemplate.update(INSERT_AUTHORITY_SQL, userId, auth.getAuthority());
    }
  }

  @Transactional
  public void insertExpirableAuthority(UUID userId, PremiumFeature role, LocalDate expiryDate) {
    jdbcTemplate.update(DELETE_EXPIRED_AUTHORITY_SQL, userId);
    jdbcTemplate.update(INSERT_EXPIRABLE_AUTHORITY_SQL, userId, ROLE_PREFIX + role, expiryDate.toString());
  }

  public void deleteUserAuthorities(UUID userId) {
    jdbcTemplate.update(DELETE_AUTHORITY_SQL, userId.toString());
  }

  public List<GrantedAuthority> getUserAuthorities(UUID userId) {
    AuthoritiesCallbackHandler callbackHandler = new AuthoritiesCallbackHandler();
    jdbcTemplate.query(GET_AUTHORITY_SQL, callbackHandler, userId.toString());
    return callbackHandler.authorities;
  }

  private static class AuthoritiesCallbackHandler implements RowCallbackHandler {

    private final List<GrantedAuthority> authorities = new ArrayList<>();

    @Override
    public void processRow(ResultSet rs) throws SQLException {
      String roleName = rs.getString(1);
      Timestamp expiryDate = rs.getTimestamp(2);

      if (expiryDate == null) {
        authorities.add(new SimpleGrantedAuthority(roleName));
      }

      if (expiryDate != null && now().isBefore(expiryDate.toLocalDateTime())) {
        authorities.add(new SimpleGrantedAuthority(roleName));
      }
    }
  }
}
