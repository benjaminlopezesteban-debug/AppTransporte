package cl.duoc.appbelmonte.viaje.service;

import cl.duoc.appbelmonte.viaje.client.ConductorClient;
import cl.duoc.appbelmonte.viaje.client.EstadoViajeClient;
import cl.duoc.appbelmonte.viaje.client.HistorialClient;
import cl.duoc.appbelmonte.viaje.client.SolicitudClient;
import cl.duoc.appbelmonte.viaje.dto.AsignacionRequest;
import cl.duoc.appbelmonte.viaje.dto.ConductorResponse;
import cl.duoc.appbelmonte.viaje.dto.HistorialRequest;
import cl.duoc.appbelmonte.viaje.dto.ReasignacionRequest;
import cl.duoc.appbelmonte.viaje.dto.SolicitudResponse;
import cl.duoc.appbelmonte.viaje.dto.ViajeResponse;
import cl.duoc.appbelmonte.viaje.exception.DatosInvalidosException;
import cl.duoc.appbelmonte.viaje.exception.RecursoNoEncontradoException;
import cl.duoc.appbelmonte.viaje.exception.ReglaNegocioException;
import cl.duoc.appbelmonte.viaje.exception.ServicioNoDisponibleException;
import cl.duoc.appbelmonte.viaje.model.Viaje;
import cl.duoc.appbelmonte.viaje.repository.ViajeRepository;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ViajeService {

	private static final Logger log = LoggerFactory.getLogger(ViajeService.class);

	private static final Long ESTADO_SOLICITUD_PENDIENTE = 1L;
	private static final Long ESTADO_ASIGNADO = 1L;
	private static final Long ESTADO_EN_CURSO = 2L;
	private static final Long ESTADO_FINALIZADO = 3L;

	private final ViajeRepository viajeRepository;
	private final SolicitudClient solicitudClient;
	private final ConductorClient conductorClient;
	private final EstadoViajeClient estadoViajeClient;
	private final HistorialClient historialClient;

	public ViajeService(ViajeRepository viajeRepository, SolicitudClient solicitudClient,
			ConductorClient conductorClient, EstadoViajeClient estadoViajeClient,
			HistorialClient historialClient) {
		this.viajeRepository = viajeRepository;
		this.solicitudClient = solicitudClient;
		this.conductorClient = conductorClient;
		this.estadoViajeClient = estadoViajeClient;
		this.historialClient = historialClient;
	}

	@Transactional
	public ViajeResponse asignar(AsignacionRequest solicitud, Long asignadoPorId) {
		SolicitudResponse solicitudActual = solicitudClient.obtener(solicitud.solicitudId());
		if (!ESTADO_SOLICITUD_PENDIENTE.equals(solicitudActual.estadoId())) {
			throw new ReglaNegocioException("La solicitud no está pendiente");
		}
		if (viajeRepository.existsBySolicitudId(solicitud.solicitudId())) {
			throw new ReglaNegocioException("La solicitud ya tiene un viaje asignado");
		}
		ConductorResponse conductor = conductorClient.obtener(solicitud.conductorId());
		if (!Boolean.TRUE.equals(conductor.disponible())) {
			throw new ReglaNegocioException("El conductor no está disponible");
		}

		Viaje viaje = new Viaje();
		viaje.setFechaAsignacion(LocalDateTime.now().withNano(0));
		viaje.setSolicitudId(solicitud.solicitudId());
		viaje.setConductorId(solicitud.conductorId());
		viaje.setAsignadoPorId(asignadoPorId);
		viaje.setEstadoId(ESTADO_ASIGNADO);
		Viaje guardado = viajeRepository.save(viaje);

		SolicitudResponse solicitudAsignada = solicitudClient.asignar(solicitud.solicitudId());
		registrarHistorial(guardado.getId(), null, ESTADO_ASIGNADO, "Viaje asignado");
		return aResponse(guardado, solicitudAsignada);
	}

	@Transactional
	public ViajeResponse reasignar(Long id, ReasignacionRequest solicitud, Long asignadoPorId) {
		Viaje viaje = buscar(id);
		if (!ESTADO_ASIGNADO.equals(viaje.getEstadoId())) {
			throw new ReglaNegocioException("Solo se puede reasignar un viaje asignado");
		}
		ConductorResponse conductor = conductorClient.obtener(solicitud.conductorId());
		if (!Boolean.TRUE.equals(conductor.disponible())) {
			throw new ReglaNegocioException("El conductor no está disponible");
		}
		if (viaje.getConductorId().equals(solicitud.conductorId())) {
			throw new ReglaNegocioException("El conductor ya está asignado a este viaje");
		}

		viaje.setConductorId(solicitud.conductorId());
		viaje.setAsignadoPorId(asignadoPorId);
		viaje.setFechaAsignacion(LocalDateTime.now().withNano(0));
		Viaje guardado = viajeRepository.save(viaje);
		registrarHistorial(guardado.getId(), ESTADO_ASIGNADO, ESTADO_ASIGNADO,
				"Conductor reasignado");
		return aResponse(guardado, solicitudClient.obtener(guardado.getSolicitudId()));
	}

	@Transactional
	public ViajeResponse iniciar(Long id, Long conductorId) {
		Viaje viaje = buscar(id);
		verificarConductor(viaje, conductorId);
		if (!ESTADO_ASIGNADO.equals(viaje.getEstadoId())) {
			throw new ReglaNegocioException("Solo se puede iniciar un viaje asignado");
		}
		viaje.setEstadoId(ESTADO_EN_CURSO);
		viaje.setHoraInicioReal(LocalDateTime.now().withNano(0));
		Viaje guardado = viajeRepository.save(viaje);
		registrarHistorial(guardado.getId(), ESTADO_ASIGNADO, ESTADO_EN_CURSO, "Viaje iniciado");
		return aResponse(guardado, solicitudClient.obtener(guardado.getSolicitudId()));
	}

	@Transactional
	public ViajeResponse finalizar(Long id, Long conductorId) {
		Viaje viaje = buscar(id);
		verificarConductor(viaje, conductorId);
		if (!ESTADO_EN_CURSO.equals(viaje.getEstadoId())) {
			throw new ReglaNegocioException("Solo se puede finalizar un viaje en curso");
		}
		viaje.setEstadoId(ESTADO_FINALIZADO);
		viaje.setHoraFinReal(LocalDateTime.now().withNano(0));
		Viaje guardado = viajeRepository.save(viaje);
		registrarHistorial(guardado.getId(), ESTADO_EN_CURSO, ESTADO_FINALIZADO,
				"Viaje finalizado");
		return aResponse(guardado, solicitudClient.obtener(guardado.getSolicitudId()));
	}

	@Transactional(readOnly = true)
	public List<ViajeResponse> listar(LocalDate fecha) {
		List<Viaje> viajes;
		if (fecha != null) {
			LocalDateTime desde = fecha.atStartOfDay();
			LocalDateTime hasta = fecha.plusDays(1).atStartOfDay();
			viajes = viajeRepository
					.findAllByFechaAsignacionGreaterThanEqualAndFechaAsignacionLessThan(desde,
							hasta);
		} else {
			viajes = viajeRepository.findAll();
		}
		return aResponses(viajes);
	}

	@Transactional(readOnly = true)
	public List<ViajeResponse> listarMios(Long conductorId) {
		return aResponses(viajeRepository.findAllByConductorId(conductorId));
	}

	@Transactional(readOnly = true)
	public List<ViajeResponse> listarHoy(Long conductorId) {
		LocalDate hoy = LocalDate.now();
		return aResponses(viajeRepository
				.findAllByConductorIdAndFechaAsignacionGreaterThanEqualAndFechaAsignacionLessThan(
						conductorId, hoy.atStartOfDay(), hoy.plusDays(1).atStartOfDay()));
	}

	@Transactional(readOnly = true)
	public ViajeResponse obtener(Long id, Long usuarioId, String rol) {
		Viaje viaje = buscar(id);
		if ("CONDUCTOR".equals(rol) && !viaje.getConductorId().equals(usuarioId)) {
			throw new AccessDeniedException("No puede ver un viaje que no le pertenece");
		}
		SolicitudResponse solicitud = solicitudClient.obtener(viaje.getSolicitudId());
		if ("SOLICITANTE".equals(rol) && !solicitud.solicitanteId().equals(usuarioId)) {
			throw new AccessDeniedException("No puede ver un viaje que no le pertenece");
		}
		return aResponse(viaje, solicitud);
	}

	private List<ViajeResponse> aResponses(List<Viaje> viajes) {
		if (viajes.isEmpty()) {
			return List.of();
		}
		List<Long> solicitudIds = viajes.stream().map(Viaje::getSolicitudId).distinct().toList();
		Map<Long, SolicitudResponse> solicitudes = solicitudClient.listarPorIds(solicitudIds)
				.stream()
				.collect(Collectors.toMap(SolicitudResponse::id, Function.identity()));
		Map<Long, String> estados = new HashMap<>();
		return viajes.stream().map(viaje -> {
			SolicitudResponse solicitud = solicitudes.get(viaje.getSolicitudId());
			if (solicitud == null) {
				throw new DatosInvalidosException("La solicitud asociada al viaje no existe");
			}
			String estadoNombre = estados.computeIfAbsent(viaje.getEstadoId(),
					idEstado -> estadoViajeClient.obtener(idEstado).nombre());
			return new ViajeResponse(viaje.getId(), viaje.getFechaAsignacion(),
					viaje.getHoraInicioReal(), viaje.getHoraFinReal(), viaje.getSolicitudId(),
					viaje.getConductorId(), viaje.getAsignadoPorId(), viaje.getEstadoId(),
					estadoNombre, solicitud);
		}).toList();
	}

	private ViajeResponse aResponse(Viaje viaje, SolicitudResponse solicitud) {
		String estadoNombre = estadoViajeClient.obtener(viaje.getEstadoId()).nombre();
		return new ViajeResponse(viaje.getId(), viaje.getFechaAsignacion(),
				viaje.getHoraInicioReal(), viaje.getHoraFinReal(), viaje.getSolicitudId(),
				viaje.getConductorId(), viaje.getAsignadoPorId(), viaje.getEstadoId(),
				estadoNombre, solicitud);
	}

	private void registrarHistorial(Long viajeId, Long estadoAnteriorId, Long estadoNuevoId,
			String detalle) {
		try {
			historialClient.registrar(new HistorialRequest(LocalDateTime.now().withNano(0),
					detalle, viajeId, estadoAnteriorId, estadoNuevoId));
		} catch (DatosInvalidosException | ServicioNoDisponibleException excepcion) {
			log.warn("No se pudo registrar el historial del viaje {}: {}", viajeId,
					excepcion.getMessage());
		}
	}

	private Viaje buscar(Long id) {
		return viajeRepository.findById(id)
				.orElseThrow(() -> new RecursoNoEncontradoException(
						"El viaje indicado no existe"));
	}

	private void verificarConductor(Viaje viaje, Long conductorId) {
		if (!viaje.getConductorId().equals(conductorId)) {
			throw new AccessDeniedException(
					"Solo el conductor del viaje puede realizar esta acción");
		}
	}
}
