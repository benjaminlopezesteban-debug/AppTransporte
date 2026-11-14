package cl.duoc.appbelmonte.historial.repository;

import cl.duoc.appbelmonte.historial.model.HistorialEstado;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface HistorialEstadoRepository extends JpaRepository<HistorialEstado, Long> {

	List<HistorialEstado> findAllByViajeIdOrderByFechaHoraAsc(Long viajeId);
}
