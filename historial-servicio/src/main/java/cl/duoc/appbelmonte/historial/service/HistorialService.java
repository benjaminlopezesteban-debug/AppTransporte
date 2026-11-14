package cl.duoc.appbelmonte.historial.service;

import cl.duoc.appbelmonte.historial.client.EstadoViajeClient;
import cl.duoc.appbelmonte.historial.client.EstadoViajeResponse;
import cl.duoc.appbelmonte.historial.dto.HistorialRequest;
import cl.duoc.appbelmonte.historial.dto.HistorialResponse;
import cl.duoc.appbelmonte.historial.model.HistorialEstado;
import cl.duoc.appbelmonte.historial.repository.HistorialEstadoRepository;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class HistorialService {

	private final HistorialEstadoRepository historialRepository;
	private final EstadoViajeClient estadoViajeClient;

	public HistorialService(HistorialEstadoRepository historialRepository,
			EstadoViajeClient estadoViajeClient) {
		this.historialRepository = historialRepository;
		this.estadoViajeClient = estadoViajeClient;
	}

	@Transactional
	public HistorialResponse registrar(HistorialRequest solicitud, Long cambiadoPorId) {
		estadoViajeClient.obtener(solicitud.estadoNuevoId());
		if (solicitud.estadoAnteriorId() != null) {
			estadoViajeClient.obtener(solicitud.estadoAnteriorId());
		}

		HistorialEstado historial = new HistorialEstado();
		historial.setFechaHora(solicitud.fechaHora() == null
				? LocalDateTime.now().withNano(0)
				: solicitud.fechaHora());
		historial.setDetalle(solicitud.detalle());
		historial.setViajeId(solicitud.viajeId());
		historial.setEstadoAnteriorId(solicitud.estadoAnteriorId());
		historial.setEstadoNuevoId(solicitud.estadoNuevoId());
		historial.setCambiadoPorId(cambiadoPorId);
		return aResponse(historialRepository.save(historial));
	}

	@Transactional(readOnly = true)
	public List<HistorialResponse> listarPorViaje(Long viajeId) {
		return historialRepository.findAllByViajeIdOrderByFechaHoraAsc(viajeId).stream()
				.map(this::aResponse)
				.toList();
	}

	private HistorialResponse aResponse(HistorialEstado historial) {
		EstadoViajeResponse estadoNuevo = estadoViajeClient.obtener(historial.getEstadoNuevoId());
		String estadoAnteriorNombre = null;
		if (historial.getEstadoAnteriorId() != null) {
			estadoAnteriorNombre = estadoViajeClient.obtener(historial.getEstadoAnteriorId())
					.nombre();
		}
		return new HistorialResponse(historial.getId(), historial.getFechaHora(),
				historial.getDetalle(), historial.getViajeId(), historial.getEstadoAnteriorId(),
				estadoAnteriorNombre, historial.getEstadoNuevoId(), estadoNuevo.nombre(),
				historial.getCambiadoPorId());
	}
}
