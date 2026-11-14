package cl.duoc.appbelmonte.solicitud.repository;

import cl.duoc.appbelmonte.solicitud.model.SolicitudTraslado;
import java.time.LocalDate;
import java.util.Collection;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SolicitudTrasladoRepository extends JpaRepository<SolicitudTraslado, Long> {

	List<SolicitudTraslado> findAllBySolicitanteId(Long solicitanteId);

	List<SolicitudTraslado> findAllByEstadoId(Long estadoId);

	List<SolicitudTraslado> findAllByFecha(LocalDate fecha);

	List<SolicitudTraslado> findAllByEstadoIdAndFecha(Long estadoId, LocalDate fecha);

	List<SolicitudTraslado> findAllByIdIn(Collection<Long> ids);
}
