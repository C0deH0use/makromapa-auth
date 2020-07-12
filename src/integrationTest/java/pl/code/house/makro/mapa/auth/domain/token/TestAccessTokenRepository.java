package pl.code.house.makro.mapa.auth.domain.token;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface TestAccessTokenRepository extends JpaRepository<AccessToken, Long> {
    @Query("SELECT ac FROM AccessToken ac WHERE ac.userId = :userId")
    List<AccessToken> findByUserId(@Param("userId") long userId);

    @Query("SELECT ac FROM AccessToken ac WHERE ac.enabled = true AND ac.userId = :userId")
    List<AccessToken> findActiveByUserId(@Param("userId") long userId);
}