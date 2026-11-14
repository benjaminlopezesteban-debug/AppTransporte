package cl.duoc.appbelmonte.estadoViaje.service;

import cl.duoc.appbelmonte.estadoViaje.dto.EstadoViajeRequest;
import cl.duoc.appbelmonte.estadoViaje.dto.EstadoViajeResponse;
import cl.duoc.appbelmonte.estadoViaje.exception.RecursoNoEncontradoException;
import cl.duoc.appbelmonte.estadoViaje.exception.ReglaNegocioException;
import cl.duoc.appbelmonte.estadoViaje.model.EstadoViaje;
import cl.duoc.appbelmonte.estadoViaje.repository.EstadoViajeRepository;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class EstadoViajeService {

	private final EstadoViajeRepository estadoViajeRepository;

	public EstadoViajeService(EstadoViajeRepository estadoViajeRepository) {
		this.estadoViajeRepository = estadoViajeRepository;
	}

	@Transactional(readOnly = true)
	public List<EstadoViajeResponse> listar() {
		return estadoViajeRepository.findAll().stream().map(this::aResponse).toList();
	}

	@Transactional(readOnly = true)
	public EstadoViajeResponse obtener(Long id) {
		return aResponse(buscar(id));
	}

	@Transactional(readOnly = true)
	public EstadoViajeResponse obtenerPorNombre(String nombre) {
		EstadoViaje estado = estadoViajeRepository.findByNombre(nombre)
				.orElseThrow(() -> new RecursoNoEncontradoException("El estado no existe"));
		return aResponse(estado);
	}

	@Transactional
	public EstadoViajeResponse crear(EstadoViajeRequest solicitud) {
		if (estadoViajeRepository.existsByNombre(solicitud.nombre())) {
			throw new ReglaNegocioException("Ya existe un estado con ese nombre");
		}
		EstadoViaje estado = new EstadoViaje();
		estado.setNombre(solicitud.nombre());
		return aResponse(estadoViajeRepository.save(estado));
	}

	@Transactional
	public EstadoViajeResponse actualizar(Long id, EstadoViajeRequest solicitud) {
		EstadoViaje estado = buscar(id);
		if (!estado.getNombre().equals(solicitud.nombre())
				&& estadoViajeRepository.existsByNombre(solicitud.nombre())) {
			throw new ReglaNegocioException("Ya existe un estado con ese nombre");
		}
		estado.setNombre(solicitud.nombre());
		return aResponse(estadoViajeRepository.save(estado));
	}

	private EstadoViaje buscar(Long id) {
		return estadoViajeRepository.findById(id)
				.orElseThrow(() -> new RecursoNoEncontradoException("El estado no existe"));
	}

	private EstadoViajeResponse aResponse(EstadoViaje estado) {
		return new EstadoViajeResponse(estado.getId(), estado.getNombre());
	}
}
