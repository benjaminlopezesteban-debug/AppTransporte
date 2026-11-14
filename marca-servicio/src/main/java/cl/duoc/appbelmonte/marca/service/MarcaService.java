package cl.duoc.appbelmonte.marca.service;

import cl.duoc.appbelmonte.marca.dto.MarcaRequest;
import cl.duoc.appbelmonte.marca.dto.MarcaResponse;
import cl.duoc.appbelmonte.marca.exception.RecursoNoEncontradoException;
import cl.duoc.appbelmonte.marca.exception.ReglaNegocioException;
import cl.duoc.appbelmonte.marca.model.Marca;
import cl.duoc.appbelmonte.marca.repository.MarcaRepository;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class MarcaService {

	private final MarcaRepository marcaRepository;

	public MarcaService(MarcaRepository marcaRepository) {
		this.marcaRepository = marcaRepository;
	}

	@Transactional(readOnly = true)
	public List<MarcaResponse> listar() {
		return marcaRepository.findAll().stream().map(this::aResponse).toList();
	}

	@Transactional(readOnly = true)
	public MarcaResponse obtener(Long id) {
		return aResponse(buscar(id));
	}

	@Transactional
	public MarcaResponse crear(MarcaRequest solicitud) {
		if (marcaRepository.existsByNombre(solicitud.nombre())) {
			throw new ReglaNegocioException("Ya existe una marca con ese nombre");
		}
		Marca marca = new Marca();
		marca.setNombre(solicitud.nombre());
		return aResponse(marcaRepository.save(marca));
	}

	@Transactional
	public MarcaResponse actualizar(Long id, MarcaRequest solicitud) {
		Marca marca = buscar(id);
		if (!marca.getNombre().equals(solicitud.nombre())
				&& marcaRepository.existsByNombre(solicitud.nombre())) {
			throw new ReglaNegocioException("Ya existe una marca con ese nombre");
		}
		marca.setNombre(solicitud.nombre());
		return aResponse(marcaRepository.save(marca));
	}

	private Marca buscar(Long id) {
		return marcaRepository.findById(id)
				.orElseThrow(() -> new RecursoNoEncontradoException("La marca no existe"));
	}

	private MarcaResponse aResponse(Marca marca) {
		return new MarcaResponse(marca.getId(), marca.getNombre());
	}
}
