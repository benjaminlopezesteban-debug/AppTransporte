package cl.duoc.appbelmonte.solicitud.service;

import cl.duoc.appbelmonte.solicitud.client.DireccionClient;
import cl.duoc.appbelmonte.solicitud.client.EstadoSolicitudClient;
import cl.duoc.appbelmonte.solicitud.client.PasajeroClient;
import cl.duoc.appbelmonte.solicitud.dto.DireccionResponse;
import cl.duoc.appbelmonte.solicitud.dto.EstadoSolicitudResponse;
import cl.duoc.appbelmonte.solicitud.dto.PasajeroResponse;
import cl.duoc.appbelmonte.solicitud.dto.SolicitudRequest;
import cl.duoc.appbelmonte.solicitud.dto.SolicitudResponse;
import cl.duoc.appbelmonte.solicitud.exception.RecursoNoEncontradoException;
import cl.duoc.appbelmonte.solicitud.exception.ReglaNegocioException;
import cl.duoc.appbelmonte.solicitud.model.SolicitudTraslado;
import cl.duoc.appbelmonte.solicitud.repository.SolicitudTrasladoRepository;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class SolicitudService {

	private static final Long ESTADO_PENDIENTE = 1L;
	private static final Long ESTADO_ASIGNADA = 2L;
	private static final Long ESTADO_CANCELADA = 3L;

	private final SolicitudTrasladoRepository solicitudRepository;
	private final PasajeroClient pasajeroClient;
	private final DireccionClient direccionClient;
	private final EstadoSolicitudClient estadoSolicitudClient;

	public SolicitudService(SolicitudTrasladoRepository solicitudRepository,
			PasajeroClient pasajeroClient, DireccionClient direccionClient,
			EstadoSolicitudClient estadoSolicitudClient) {
		this.solicitudRepository = solicitudRepository;
		this.pasajeroClient = pasajeroClient;
		this.direccionClient = direccionClient;
		this.estadoSolicitudClient = estadoSolicitudClient;
	}

	@Transactional
	public SolicitudResponse crear(SolicitudRequest solicitud, Long solicitanteId) {
		EstadoSolicitudResponse estado = estadoSolicitudClient.obtener(ESTADO_PENDIENTE);
		PasajeroResponse pasajero = pasajeroClient.crear(solicitud.pasajero());
		DireccionResponse origen = direccionClient.crear(solicitud.origen());
		DireccionResponse destino = direccionClient.crear(solicitud.destino());

		SolicitudTraslado entidad = new SolicitudTraslado();
		entidad.setFecha(solicitud.fecha());
		entidad.setHora(solicitud.hora());
		entidad.setNumeroVuelo(solicitud.numeroVuelo());
		entidad.setObservaciones(solicitud.observaciones());
		entidad.setFechaCreacion(LocalDateTime.now().withNano(0));
		entidad.setSolicitanteId(solicitanteId);
		entidad.setPasajeroId(pasajero.id());
		entidad.setOrigenId(origen.id());
		entidad.setDestinoId(destino.id());
		entidad.setEstadoId(ESTADO_PENDIENTE);

		SolicitudTraslado guardada = solicitudRepository.save(entidad);
		return new SolicitudResponse(guardada.getId(), guardada.getFecha(), guardada.getHora(),
				guardada.getNumeroVuelo(), guardada.getObservaciones(),
				guardada.getFechaCreacion(), estado.id(), estado.nombre(),
				guardada.getSolicitanteId(), pasajero, origen, destino);
	}

	@Transactional(readOnly = true)
	public List<SolicitudResponse> listarMias(Long solicitanteId) {
		return solicitudRepository.findAllBySolicitanteId(solicitanteId).stream()
				.map(this::aResponse)
				.toList();
	}

	@Transactional
	public SolicitudResponse cancelar(Long id) {
		SolicitudTraslado solicitud = buscar(id);
		if (!ESTADO_PENDIENTE.equals(solicitud.getEstadoId())) {
			throw new ReglaNegocioException("Solo se puede cancelar una solicitud pendiente");
		}
		solicitud.setEstadoId(ESTADO_CANCELADA);
		return aResponse(solicitudRepository.save(solicitud));
	}

	@Transactional(readOnly = true)
	public List<SolicitudResponse> listar(Long estadoId, LocalDate fecha) {
		List<SolicitudTraslado> solicitudes;
		if (estadoId != null && fecha != null) {
			solicitudes = solicitudRepository.findAllByEstadoIdAndFecha(estadoId, fecha);
		} else if (estadoId != null) {
			solicitudes = solicitudRepository.findAllByEstadoId(estadoId);
		} else if (fecha != null) {
			solicitudes = solicitudRepository.findAllByFecha(fecha);
		} else {
			solicitudes = solicitudRepository.findAll();
		}
		return solicitudes.stream().map(this::aResponse).toList();
	}

	@Transactional(readOnly = true)
	public List<SolicitudResponse> listarPorIds(List<Long> ids, LocalDate fecha) {
		return solicitudRepository.findAllByIdIn(ids).stream()
				.filter(solicitud -> fecha == null || fecha.equals(solicitud.getFecha()))
				.map(this::aResponse)
				.toList();
	}

	@Transactional(readOnly = true)
	public SolicitudResponse obtener(Long id) {
		return aResponse(buscar(id));
	}

	@Transactional
	public SolicitudResponse asignar(Long id) {
		SolicitudTraslado solicitud = buscar(id);
		if (!ESTADO_PENDIENTE.equals(solicitud.getEstadoId())) {
			throw new ReglaNegocioException("Solo se puede asignar una solicitud pendiente");
		}
		solicitud.setEstadoId(ESTADO_ASIGNADA);
		return aResponse(solicitudRepository.save(solicitud));
	}

	private SolicitudTraslado buscar(Long id) {
		return solicitudRepository.findById(id)
				.orElseThrow(() -> new RecursoNoEncontradoException(
						"La solicitud indicada no existe"));
	}

	private SolicitudResponse aResponse(SolicitudTraslado solicitud) {
		EstadoSolicitudResponse estado = estadoSolicitudClient.obtener(solicitud.getEstadoId());
		PasajeroResponse pasajero = pasajeroClient.obtener(solicitud.getPasajeroId());
		DireccionResponse origen = direccionClient.obtener(solicitud.getOrigenId());
		DireccionResponse destino = direccionClient.obtener(solicitud.getDestinoId());
		return new SolicitudResponse(solicitud.getId(), solicitud.getFecha(), solicitud.getHora(),
				solicitud.getNumeroVuelo(), solicitud.getObservaciones(),
				solicitud.getFechaCreacion(), estado.id(), estado.nombre(),
				solicitud.getSolicitanteId(), pasajero, origen, destino);
	}
}
