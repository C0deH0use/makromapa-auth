package pl.code.house.makro.mapa.auth.domain.user;

import static java.time.ZoneOffset.UTC;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.then;
import static pl.code.house.makro.mapa.auth.domain.user.PremiumFeature.DISABLE_ADS;

import java.sql.Timestamp;
import java.time.Clock;
import java.time.Instant;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowCallbackHandler;
import pl.code.house.makro.mapa.auth.domain.user.UserAuthoritiesService.AuthoritiesCallbackHandler;

@ExtendWith(MockitoExtension.class)
class UserAuthoritiesServiceTest {

  private static final UUID userId = UUID.fromString("e626ba90-fbef-48ff-af48-afb1f378ed8b");

  Clock clock = Clock.fixed(Instant.parse("2021-06-03T10:15:30.00Z"), UTC);

  @Mock
  JdbcTemplate jdbcTemplate;

  UserAuthoritiesService sut;

  @BeforeEach
  void setup() {
    sut = new UserAuthoritiesService(clock, jdbcTemplate);
  }

  @Test
  @DisplayName("should correctly insert new premium authority with proper expiry date")
  void shouldCorrectlyInsertNewPremiumAuthorityWithProperExpiryDate() {
    //when
    sut.insertExpirableAuthority(userId, DISABLE_ADS, 1);

    //then
    then(jdbcTemplate).should().query(eq("SELECT user_id, authority, expiry_date FROM user_authority WHERE (expiry_date IS NULL OR expiry_date > now()) AND user_id::text = ?"), any(AuthoritiesCallbackHandler.class), eq(userId.toString()));
    then(jdbcTemplate).should().update("DELETE FROM user_authority WHERE expiry_date < now() AND user_id::text = ?", userId.toString());
    then(jdbcTemplate).should().update("INSERT INTO user_authority (user_id, authority, expiry_date) values (?,?,?)", userId, "ROLE_DISABLE_ADS", Timestamp.valueOf("2021-06-10 11:00:00.0"));

    //and
    then(jdbcTemplate).should().query(
        eq("SELECT user_id, authority, expiry_date FROM user_authority WHERE (expiry_date IS NULL OR expiry_date > now()) AND user_id::text = ?"),
        any(RowCallbackHandler.class),
        eq("e626ba90-fbef-48ff-af48-afb1f378ed8b"));
  }

  @Test
  @DisplayName("should correctly insert new premium without expiration date")
  void shouldCorrectlyInsertNewPremiumWithoutExpirationDate() {
    //when
    sut.insertExpirableAuthority(userId, DISABLE_ADS, 0);

    //them
    then(jdbcTemplate).should().query(eq("SELECT user_id, authority, expiry_date FROM user_authority WHERE (expiry_date IS NULL OR expiry_date > now()) AND user_id::text = ?"), any(AuthoritiesCallbackHandler.class), eq(userId.toString()));
    then(jdbcTemplate).should().update("DELETE FROM user_authority WHERE expiry_date < now() AND user_id::text = ?", userId.toString());
    then(jdbcTemplate).should().update("INSERT INTO user_authority (user_id, authority) values (?,?)", userId, "ROLE_DISABLE_ADS");

    //and
    then(jdbcTemplate).should().query(
        eq("SELECT user_id, authority, expiry_date FROM user_authority WHERE (expiry_date IS NULL OR expiry_date > now()) AND user_id::text = ?"),
        any(RowCallbackHandler.class),
        eq("e626ba90-fbef-48ff-af48-afb1f378ed8b"));
  }

}