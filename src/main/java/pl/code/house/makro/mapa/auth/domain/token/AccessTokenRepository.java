package pl.code.house.makro.mapa.auth.domain.token;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
interface AccessTokenRepository extends JpaRepository<AccessToken, Long> {

  @Query("SELECT ac FROM AccessToken ac WHERE ac.code = :accessCode")
  Optional<AccessToken> findByAccessCode(@Param("accessCode") String accessCode);

  @Modifying(flushAutomatically = true)
  @Query("UPDATE AccessToken ac SET ac.enabled = false WHERE ac.userId = :userId")
  int revokeTokensForUserId(@Param("userId") long userId);
}
