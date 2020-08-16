package pl.code.house.makro.mapa.auth.domain.user;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
interface UserVerificationCodeRepository extends JpaRepository<UserVerificationCode, UUID> {

  @Query("SELECT ac FROM UserVerificationCode ac "
      + "WHERE ac.expiresOn >= :now "
      + "AND ac.enabled = true "
      + "AND ac.code = :activationCode "
      + "AND ac.codeType = :codeType")
  Optional<UserVerificationCode> findActiveCode(
      @Param("activationCode") String activationCode,
      @Param("now") ZonedDateTime now,
      @Param("codeType") CodeType codeType
  );

  @Query("SELECT ac FROM UserVerificationCode ac WHERE ac.user.id = :userId AND ac.codeType = :codeType")
  List<UserVerificationCode> findByUserIdAndCodeType(@Param("userId") UUID userId, @Param("codeType") CodeType codeType);

  @Modifying(flushAutomatically = true)
  @Query("UPDATE UserVerificationCode ac SET ac.enabled = false WHERE ac.id = :code_id")
  void useCode(@Param("code_id") UUID codeId);
}
