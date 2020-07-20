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
}
