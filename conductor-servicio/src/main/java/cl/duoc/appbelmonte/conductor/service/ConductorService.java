package cl.duoc.appbelmonte.conductor.service;

import cl.duoc.appbelmonte.conductor.client.VehiculoClient;
import cl.duoc.appbelmonte.conductor.client.VehiculoResponse;
import cl.duoc.appbelmonte.conductor.dto.ConductorRequest;
import cl.duoc.appbelmonte.conductor.dto.ConductorResponse;
import cl.duoc.appbelmonte.conductor.exception.RecursoNoEncontradoException;
import cl.duoc.appbelmonte.conductor.exception.ReglaNegocioException;
import cl.duoc.appbelmonte.conductor.model.Conductor;
import cl.duoc.appbelmonte.conductor.repository.ConductorRepository;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ConductorService {

	private final ConductorRepository conductorRepository;
	private final VehiculoClient vehiculoClient;

	public ConductorService(ConductorRepository conductorRepository,
			VehiculoClient vehiculoClient) {
		this.conductorRepository = conductorRepository;
		this.vehiculoClient = vehiculoClient;
	}

	@Transactional
	public ConductorResponse crear(Long conductorId, ConductorRequest solicitud) {
		if (conductorRepository.existsById(conductorId)) {
			throw new ReglaNegocioException("El conductor ya tiene un perfil registrado");
		}
		VehiculoResponse vehiculo = vehiculoClient.obtener(solicitud.vehiculoId());
		Conductor conductor = new Conductor();
		conductor.setId(conductorId);
		conductor.setNumeroLicencia(solicitud.numeroLicencia());
		conductor.setDisponible(true);
		conductor.setVehiculoId(vehiculo.id());
		return aResponse(conductorRepository.save(conductor));
	}

	@Transactional
	public ConductorResponse cambiarDisponibilidad(Long conductorId,
			Boolean disponible) {
		Conductor conductor = buscar(conductorId);
		conductor.setDisponible(disponible);
		return aResponse(conductorRepository.save(conductor));
	}

	@Transactional(readOnly = true)
	public List<ConductorResponse> listarDisponibles() {
		return conductorRepository.findAllByDisponibleTrue().stream()
				.map(this::aResponse)
				.toList();
	}

	@Transactional(readOnly = true)
	public ConductorResponse obtener(Long id) {
		return aResponse(buscar(id));
	}

	private Conductor buscar(Long id) {
		return conductorRepository.findById(id)
				.orElseThrow(() -> new RecursoNoEncontradoException("El conductor no existe"));
	}

	private ConductorResponse aResponse(Conductor conductor) {
		VehiculoResponse vehiculo = vehiculoClient.obtener(conductor.getVehiculoId());
		return new ConductorResponse(conductor.getId(), conductor.getNumeroLicencia(),
				conductor.getDisponible(), conductor.getVehiculoId(), vehiculo.patente(),
				vehiculo.modeloNombre(), vehiculo.marcaNombre());
	}
}
