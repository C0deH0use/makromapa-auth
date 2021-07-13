package pl.code.house.makro.mapa.auth.domain.product;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
interface ProductActionLogRepository extends JpaRepository<ProductActionLog, Long> {

}
