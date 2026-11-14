package cl.duoc.appbelmonte.estadoViaje.repository;

import cl.duoc.appbelmonte.estadoViaje.model.EstadoViaje;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EstadoViajeRepository extends JpaRepository<EstadoViaje, Long> {

	Optional<EstadoViaje> findByNombre(String nombre);

	boolean existsByNombre(String nombre);
}
