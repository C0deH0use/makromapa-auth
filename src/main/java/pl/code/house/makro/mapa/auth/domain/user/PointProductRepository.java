package pl.code.house.makro.mapa.auth.domain.user;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
interface PointProductRepository extends JpaRepository<PointsProduct, Long> {
}
