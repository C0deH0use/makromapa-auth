package pl.code.house.makro.mapa.auth.domain.token;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TestOAuthAccessTokenRepository extends JpaRepository<OAuth2AccessToken, Long> {

}
