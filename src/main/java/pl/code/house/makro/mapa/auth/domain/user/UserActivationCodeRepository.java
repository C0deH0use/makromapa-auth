package pl.code.house.makro.mapa.auth.domain.user;

import java.time.ZonedDateTime;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
interface UserActivationCodeRepository extends JpaRepository<UserActivationCode, UUID> {

  @Query("SELECT ac FROM UserActivationCode ac WHERE ac.expiresOn >= :now AND ac.enabled = true AND ac.code = :activationCode")
  Optional<UserActivationCode> findActiveCode(@Param("activationCode") String activationCode, @Param("now") ZonedDateTime now);

  @Query("SELECT ac FROM UserActivationCode ac WHERE ac.draftUser.id = :userId")
  Optional<UserActivationCode> findByUserId(@Param("userId") UUID userId);

  @Modifying(flushAutomatically = true)
  @Query("UPDATE UserActivationCode ac SET ac.enabled = false WHERE ac.id = :code_id")
  void useCode(@Param("code_id") UUID codeId);
}
