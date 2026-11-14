package cl.duoc.appbelmonte.comuna.repository;

import cl.duoc.appbelmonte.comuna.model.Comuna;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ComunaRepository extends JpaRepository<Comuna, Long> {

	List<Comuna> findAllByRegionId(Long regionId);
}
