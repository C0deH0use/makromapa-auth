package pl.code.house.makro.mapa.auth.domain.user;

import static java.util.stream.Collectors.toList;
import static pl.code.house.makro.mapa.auth.domain.user.UserType.PREMIUM_USER;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import javax.sql.DataSource;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserAuthoritiesService {

  private static final String GET_USER_AUTHORITY_SQL = "SELECT authority FROM user_authority WHERE user_id::text = ?";
  private static final String INSERT_AUTHORITY_SQL = "INSERT INTO user_authority (user_id, authority) values (?,?)";
  private static final String DELETE_AUTHORITY_SQL = "DELETE FROM user_authority WHERE user_id::text = ?";

  private final JdbcTemplate jdbcTemplate;

  public UserAuthoritiesService(DataSource dataSource) {
    this.jdbcTemplate = new JdbcTemplate(dataSource);
  }

  public static List<GrantedAuthority> userAuthoritiesFor(UserType type) {
    List<GrantedAuthority> authorities = new ArrayList<>();
    if (PREMIUM_USER == type) {
      authorities.add(new SimpleGrantedAuthority("ROLE_PREMIUM_USER"));
    }
    return authorities;
  }

  @Transactional
  public void insertUserAuthorities(UUID userId, UserType type) {
    deleteUserAuthorities(userId);

    for (GrantedAuthority auth : userAuthoritiesFor(type)) {
      jdbcTemplate.update(INSERT_AUTHORITY_SQL, userId, auth.getAuthority());
    }
  }

  @Transactional
  public List<GrantedAuthority> userAuthorities(UUID userId) {
    List<String> authorities = jdbcTemplate.queryForList(GET_USER_AUTHORITY_SQL, new String[]{userId.toString()}, String.class);

    return authorities.stream()
        .map(SimpleGrantedAuthority::new)
        .collect(toList());
  }

  public void deleteUserAuthorities(UUID userId) {
    jdbcTemplate.update(DELETE_AUTHORITY_SQL, userId.toString());
  }
}
