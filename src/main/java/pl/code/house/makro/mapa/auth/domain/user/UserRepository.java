package pl.code.house.makro.mapa.auth.domain.user;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
interface UserRepository extends JpaRepository<User, Long> {

  @Query("SELECT u FROM User u WHERE u.externalId = :externalId")
  Optional<User> findByExternalIdAndAuthProvider(@Param("externalId") String externalId);

  @Query("SELECT count(u) > 0 FROM User u WHERE u.provider = 'BASIC_AUTH' AND u.userDetails.email = :userEmail AND (u.userDetails.type = 'DRAFT_USER' OR u.userDetails.type = 'FREE_USER')")
  boolean existsByBasicAuthAndUserEmail(@Param("userEmail") String userEmail);
}
