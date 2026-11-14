package cl.duoc.appbelmonte.region.service;

import cl.duoc.appbelmonte.region.dto.RegionRequest;
import cl.duoc.appbelmonte.region.dto.RegionResponse;
import cl.duoc.appbelmonte.region.exception.RecursoNoEncontradoException;
import cl.duoc.appbelmonte.region.exception.ReglaNegocioException;
import cl.duoc.appbelmonte.region.model.Region;
import cl.duoc.appbelmonte.region.repository.RegionRepository;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class RegionService {

	private final RegionRepository regionRepository;

	public RegionService(RegionRepository regionRepository) {
		this.regionRepository = regionRepository;
	}

	@Transactional(readOnly = true)
	public List<RegionResponse> listar() {
		return regionRepository.findAll().stream().map(this::aResponse).toList();
	}

	@Transactional(readOnly = true)
	public RegionResponse obtener(Long id) {
		return aResponse(buscar(id));
	}

	@Transactional
	public RegionResponse crear(RegionRequest solicitud) {
		if (regionRepository.existsByNombre(solicitud.nombre())) {
			throw new ReglaNegocioException("Ya existe una región con ese nombre");
		}
		Region region = new Region();
		region.setNombre(solicitud.nombre());
		return aResponse(regionRepository.save(region));
	}

	@Transactional
	public RegionResponse actualizar(Long id, RegionRequest solicitud) {
		Region region = buscar(id);
		if (!region.getNombre().equals(solicitud.nombre())
				&& regionRepository.existsByNombre(solicitud.nombre())) {
			throw new ReglaNegocioException("Ya existe una región con ese nombre");
		}
		region.setNombre(solicitud.nombre());
		return aResponse(regionRepository.save(region));
	}

	private Region buscar(Long id) {
		return regionRepository.findById(id)
				.orElseThrow(() -> new RecursoNoEncontradoException("La región no existe"));
	}

	private RegionResponse aResponse(Region region) {
		return new RegionResponse(region.getId(), region.getNombre());
	}
}
