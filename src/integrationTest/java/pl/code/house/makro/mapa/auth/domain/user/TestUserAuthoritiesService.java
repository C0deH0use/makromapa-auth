package pl.code.house.makro.mapa.auth.domain.user;

import static org.assertj.core.api.Assertions.assertThat;

import io.vavr.Tuple;
import io.vavr.Tuple2;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.Clock;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.assertj.core.api.ListAssert;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowCallbackHandler;
import org.springframework.stereotype.Service;

@Service
public class TestUserAuthoritiesService {

  static final String GET_AUTHORITY_SQL = "SELECT user_id, authority, expiry_date FROM user_authority "
      + "WHERE user_id::text = ?";
  private static final String INSERT_AUTHORITY_SQL = "INSERT INTO user_authority (user_id, authority) values (?,?)";
  private static final String INSERT_EXPIRABLE_AUTHORITY_SQL = "INSERT INTO user_authority (user_id, authority, expiry_date) values (?,?,?)";
  private static final String DELETE_AUTHORITY_SQL = "DELETE FROM user_authority WHERE user_id::text = ?";
  private static final String DELETE_EXPIRED_AUTHORITY_SQL = "DELETE FROM user_authority WHERE user_id::text = ?";

  private final Clock clock;
  private final JdbcTemplate jdbcTemplate;

  TestUserAuthoritiesService(Clock clock, JdbcTemplate jdbcTemplate) {
    this.clock = clock;
    this.jdbcTemplate = jdbcTemplate;
  }

  List<Tuple2<String, LocalDateTime>> getUserAuthorities(UUID userId) {
    AuthoritiesCallbackHandler callbackHandler = new AuthoritiesCallbackHandler();
    jdbcTemplate.query(GET_AUTHORITY_SQL, callbackHandler, userId.toString());
    return callbackHandler.authorities;
  }

  public ListAssert<Tuple2<String, LocalDateTime>> assertUserRoles(UUID userId) {
    return assertThat(getUserAuthorities(userId));
  }

  public ListAssert<Tuple2<String, LocalDateTime>> assertUserFeatureRoles(UUID userId) {
    return assertThat(getUserAuthorities(userId))
        .filteredOn(tuple -> !tuple._1.equalsIgnoreCase("ROLE_FREE_USER"))
        .filteredOn(tuple -> !tuple._1.equalsIgnoreCase("ROLE_ADMIN_USER"));
  }

  @RequiredArgsConstructor
  private static class AuthoritiesCallbackHandler implements RowCallbackHandler {

    private final List<Tuple2<String, LocalDateTime>> authorities = new ArrayList<>();

    @Override
    public void processRow(ResultSet rs) throws SQLException {
      String roleName = rs.getString(2);
      LocalDateTime expiryDate = Optional.ofNullable(rs.getTimestamp(3))
          .map(Timestamp::toLocalDateTime)
          .orElse(LocalDateTime.MAX);


      authorities.add(Tuple.of(roleName, expiryDate));
    }
  }
}