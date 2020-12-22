package pl.code.house.makro.mapa.auth.domain.user;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface TestUserRepository extends JpaRepository<BaseUser, UUID> {
  @Query("SELECT COUNT(u) FROM BaseUser u "
      + "WHERE u.externalId = :externalId")
  Long countByExternalId(@Param("externalId") String externalId);

}