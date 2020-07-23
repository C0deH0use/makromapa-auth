package pl.code.house.makro.mapa.auth.domain.user;

import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
interface UserActivationCodeRepository extends JpaRepository<UserActivationCode, UUID> {

  @Query("SELECT ac FROM UserActivationCode ac WHERE p <= CURRENT_TIMESTAMP AND ac.code = :activationCode")
  Optional<UserActivationCode> findActiveCode(@Param("activationCode") String activationCode);

  @Query("SELECT ac FROM UserActivationCode ac WHERE ac.enabled = true AND ac.draftUser.id = :userId")
  Optional<UserActivationCode> findActiveCodeByUserId(@Param("userId") UUID userId);

}
