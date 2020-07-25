package pl.code.house.makro.mapa.auth.domain.user;

import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
interface UserRepository extends JpaRepository<BaseUser, UUID> {

  @Query("SELECT u FROM BaseUser u WHERE u.externalId = :externalId")
  Optional<BaseUser> findByExternalIdAndAuthProvider(@Param("externalId") String externalId);

  @Query("SELECT u FROM BaseUser u "
      + "WHERE u.provider = 'BASIC_AUTH' "
      + "AND u.userDetails.email = :userEmail "
      + "AND (u.userDetails.type = 'DRAFT_USER' OR u.userDetails.type = 'FREE_USER')")
  Optional<BaseUser> findUserWithPasswordByUserEmail(@Param("userEmail") String userEmail);
}
