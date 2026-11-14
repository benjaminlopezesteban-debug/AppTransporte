package cl.duoc.appbelmonte.viaje.repository;

import cl.duoc.appbelmonte.viaje.model.Viaje;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ViajeRepository extends JpaRepository<Viaje, Long> {

	boolean existsBySolicitudId(Long solicitudId);

	List<Viaje> findAllByConductorId(Long conductorId);

	List<Viaje> findAllByFechaAsignacionGreaterThanEqualAndFechaAsignacionLessThan(
			LocalDateTime desde, LocalDateTime hasta);

	List<Viaje> findAllByConductorIdAndFechaAsignacionGreaterThanEqualAndFechaAsignacionLessThan(
			Long conductorId, LocalDateTime desde, LocalDateTime hasta);
}
