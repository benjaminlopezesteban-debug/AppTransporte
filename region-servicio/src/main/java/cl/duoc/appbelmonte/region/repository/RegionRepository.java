package cl.duoc.appbelmonte.region.repository;

import cl.duoc.appbelmonte.region.model.Region;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RegionRepository extends JpaRepository<Region, Long> {

	Optional<Region> findByNombre(String nombre);

	boolean existsByNombre(String nombre);
}
