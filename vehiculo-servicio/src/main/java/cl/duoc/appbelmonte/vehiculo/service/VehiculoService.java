package cl.duoc.appbelmonte.vehiculo.service;

import cl.duoc.appbelmonte.vehiculo.client.ModeloVehiculoClient;
import cl.duoc.appbelmonte.vehiculo.client.ModeloVehiculoResponse;
import cl.duoc.appbelmonte.vehiculo.dto.VehiculoRequest;
import cl.duoc.appbelmonte.vehiculo.dto.VehiculoResponse;
import cl.duoc.appbelmonte.vehiculo.exception.RecursoNoEncontradoException;
import cl.duoc.appbelmonte.vehiculo.exception.ReglaNegocioException;
import cl.duoc.appbelmonte.vehiculo.model.Vehiculo;
import cl.duoc.appbelmonte.vehiculo.repository.VehiculoRepository;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class VehiculoService {

	private final VehiculoRepository vehiculoRepository;
	private final ModeloVehiculoClient modeloClient;

	public VehiculoService(VehiculoRepository vehiculoRepository,
			ModeloVehiculoClient modeloClient) {
		this.vehiculoRepository = vehiculoRepository;
		this.modeloClient = modeloClient;
	}

	@Transactional(readOnly = true)
	public List<VehiculoResponse> listar() {
		return vehiculoRepository.findAll().stream().map(this::aResponse).toList();
	}

	@Transactional(readOnly = true)
	public VehiculoResponse obtener(Long id) {
		return aResponse(buscar(id));
	}

	@Transactional
	public VehiculoResponse crear(VehiculoRequest solicitud) {
		if (vehiculoRepository.existsByPatente(solicitud.patente())) {
			throw new ReglaNegocioException("La patente ya está registrada");
		}
		ModeloVehiculoResponse modelo = modeloClient.obtener(solicitud.modeloId());
		Vehiculo vehiculo = new Vehiculo();
		vehiculo.setPatente(solicitud.patente());
		vehiculo.setAnio(solicitud.anio());
		vehiculo.setModeloId(modelo.id());
		return aResponse(vehiculoRepository.save(vehiculo));
	}

	@Transactional
	public VehiculoResponse actualizar(Long id, VehiculoRequest solicitud) {
		Vehiculo vehiculo = buscar(id);
		if (vehiculoRepository.existsByPatenteAndIdNot(solicitud.patente(), id)) {
			throw new ReglaNegocioException("La patente ya está registrada");
		}
		ModeloVehiculoResponse modelo = modeloClient.obtener(solicitud.modeloId());
		vehiculo.setPatente(solicitud.patente());
		vehiculo.setAnio(solicitud.anio());
		vehiculo.setModeloId(modelo.id());
		return aResponse(vehiculoRepository.save(vehiculo));
	}

	private Vehiculo buscar(Long id) {
		return vehiculoRepository.findById(id)
				.orElseThrow(() -> new RecursoNoEncontradoException("El vehículo no existe"));
	}

	private VehiculoResponse aResponse(Vehiculo vehiculo) {
		ModeloVehiculoResponse modelo = modeloClient.obtener(vehiculo.getModeloId());
		return new VehiculoResponse(vehiculo.getId(), vehiculo.getPatente(), vehiculo.getAnio(),
				vehiculo.getModeloId(), modelo.nombre(), modelo.marcaNombre());
	}
}
