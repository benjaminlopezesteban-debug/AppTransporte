package cl.duoc.appbelmonte.estadoSolicitud.service;

import cl.duoc.appbelmonte.estadoSolicitud.dto.EstadoSolicitudRequest;
import cl.duoc.appbelmonte.estadoSolicitud.dto.EstadoSolicitudResponse;
import cl.duoc.appbelmonte.estadoSolicitud.exception.RecursoNoEncontradoException;
import cl.duoc.appbelmonte.estadoSolicitud.exception.ReglaNegocioException;
import cl.duoc.appbelmonte.estadoSolicitud.model.EstadoSolicitud;
import cl.duoc.appbelmonte.estadoSolicitud.repository.EstadoSolicitudRepository;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class EstadoSolicitudService {

	private final EstadoSolicitudRepository estadoSolicitudRepository;

	public EstadoSolicitudService(EstadoSolicitudRepository estadoSolicitudRepository) {
		this.estadoSolicitudRepository = estadoSolicitudRepository;
	}

	@Transactional(readOnly = true)
	public List<EstadoSolicitudResponse> listar() {
		return estadoSolicitudRepository.findAll().stream().map(this::aResponse).toList();
	}

	@Transactional(readOnly = true)
	public EstadoSolicitudResponse obtener(Long id) {
		return aResponse(buscar(id));
	}

	@Transactional(readOnly = true)
	public EstadoSolicitudResponse obtenerPorNombre(String nombre) {
		EstadoSolicitud estado = estadoSolicitudRepository.findByNombre(nombre)
				.orElseThrow(() -> new RecursoNoEncontradoException("El estado no existe"));
		return aResponse(estado);
	}

	@Transactional
	public EstadoSolicitudResponse crear(EstadoSolicitudRequest solicitud) {
		if (estadoSolicitudRepository.existsByNombre(solicitud.nombre())) {
			throw new ReglaNegocioException("Ya existe un estado con ese nombre");
		}
		EstadoSolicitud estado = new EstadoSolicitud();
		estado.setNombre(solicitud.nombre());
		return aResponse(estadoSolicitudRepository.save(estado));
	}

	@Transactional
	public EstadoSolicitudResponse actualizar(Long id, EstadoSolicitudRequest solicitud) {
		EstadoSolicitud estado = buscar(id);
		if (!estado.getNombre().equals(solicitud.nombre())
				&& estadoSolicitudRepository.existsByNombre(solicitud.nombre())) {
			throw new ReglaNegocioException("Ya existe un estado con ese nombre");
		}
		estado.setNombre(solicitud.nombre());
		return aResponse(estadoSolicitudRepository.save(estado));
	}

	private EstadoSolicitud buscar(Long id) {
		return estadoSolicitudRepository.findById(id)
				.orElseThrow(() -> new RecursoNoEncontradoException("El estado no existe"));
	}

	private EstadoSolicitudResponse aResponse(EstadoSolicitud estado) {
		return new EstadoSolicitudResponse(estado.getId(), estado.getNombre());
	}
}
