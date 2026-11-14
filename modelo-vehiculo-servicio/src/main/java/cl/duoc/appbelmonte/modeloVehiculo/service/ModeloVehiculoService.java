package cl.duoc.appbelmonte.modeloVehiculo.service;

import cl.duoc.appbelmonte.modeloVehiculo.client.MarcaClient;
import cl.duoc.appbelmonte.modeloVehiculo.client.MarcaResponse;
import cl.duoc.appbelmonte.modeloVehiculo.dto.ModeloVehiculoRequest;
import cl.duoc.appbelmonte.modeloVehiculo.dto.ModeloVehiculoResponse;
import cl.duoc.appbelmonte.modeloVehiculo.exception.RecursoNoEncontradoException;
import cl.duoc.appbelmonte.modeloVehiculo.model.ModeloVehiculo;
import cl.duoc.appbelmonte.modeloVehiculo.repository.ModeloVehiculoRepository;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ModeloVehiculoService {

	private final ModeloVehiculoRepository modeloVehiculoRepository;
	private final MarcaClient marcaClient;

	public ModeloVehiculoService(ModeloVehiculoRepository modeloVehiculoRepository,
			MarcaClient marcaClient) {
		this.modeloVehiculoRepository = modeloVehiculoRepository;
		this.marcaClient = marcaClient;
	}

	@Transactional(readOnly = true)
	public List<ModeloVehiculoResponse> listar(Long marcaId) {
		List<ModeloVehiculo> modelos = marcaId == null
				? modeloVehiculoRepository.findAll()
				: modeloVehiculoRepository.findAllByMarcaId(marcaId);
		return modelos.stream().map(this::aResponse).toList();
	}

	@Transactional(readOnly = true)
	public ModeloVehiculoResponse obtener(Long id) {
		return aResponse(buscar(id));
	}

	@Transactional
	public ModeloVehiculoResponse crear(ModeloVehiculoRequest solicitud) {
		MarcaResponse marca = marcaClient.obtener(solicitud.marcaId());
		ModeloVehiculo modelo = new ModeloVehiculo();
		modelo.setNombre(solicitud.nombre());
		modelo.setMarcaId(marca.id());
		return aResponse(modeloVehiculoRepository.save(modelo));
	}

	@Transactional
	public ModeloVehiculoResponse actualizar(Long id, ModeloVehiculoRequest solicitud) {
		ModeloVehiculo modelo = buscar(id);
		MarcaResponse marca = marcaClient.obtener(solicitud.marcaId());
		modelo.setNombre(solicitud.nombre());
		modelo.setMarcaId(marca.id());
		return aResponse(modeloVehiculoRepository.save(modelo));
	}

	private ModeloVehiculo buscar(Long id) {
		return modeloVehiculoRepository.findById(id)
				.orElseThrow(() -> new RecursoNoEncontradoException("El modelo no existe"));
	}

	private ModeloVehiculoResponse aResponse(ModeloVehiculo modelo) {
		MarcaResponse marca = marcaClient.obtener(modelo.getMarcaId());
		return new ModeloVehiculoResponse(modelo.getId(), modelo.getNombre(),
				modelo.getMarcaId(), marca.nombre());
	}
}
