package cl.duoc.appbelmonte.estadoSolicitud.repository;

import cl.duoc.appbelmonte.estadoSolicitud.model.EstadoSolicitud;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EstadoSolicitudRepository extends JpaRepository<EstadoSolicitud, Long> {

	Optional<EstadoSolicitud> findByNombre(String nombre);

	boolean existsByNombre(String nombre);
}
