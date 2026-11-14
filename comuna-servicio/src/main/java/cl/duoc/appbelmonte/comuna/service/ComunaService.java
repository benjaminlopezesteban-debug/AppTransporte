package cl.duoc.appbelmonte.comuna.service;

import cl.duoc.appbelmonte.comuna.client.RegionClient;
import cl.duoc.appbelmonte.comuna.client.RegionResponse;
import cl.duoc.appbelmonte.comuna.dto.ComunaRequest;
import cl.duoc.appbelmonte.comuna.dto.ComunaResponse;
import cl.duoc.appbelmonte.comuna.exception.RecursoNoEncontradoException;
import cl.duoc.appbelmonte.comuna.model.Comuna;
import cl.duoc.appbelmonte.comuna.repository.ComunaRepository;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ComunaService {

	private final ComunaRepository comunaRepository;
	private final RegionClient regionClient;

	public ComunaService(ComunaRepository comunaRepository, RegionClient regionClient) {
		this.comunaRepository = comunaRepository;
		this.regionClient = regionClient;
	}

	@Transactional(readOnly = true)
	public List<ComunaResponse> listar(Long regionId) {
		List<Comuna> comunas = regionId == null
				? comunaRepository.findAll()
				: comunaRepository.findAllByRegionId(regionId);
		return comunas.stream().map(this::aResponse).toList();
	}

	@Transactional(readOnly = true)
	public ComunaResponse obtener(Long id) {
		return aResponse(buscar(id));
	}

	@Transactional
	public ComunaResponse crear(ComunaRequest solicitud) {
		RegionResponse region = regionClient.obtener(solicitud.regionId());
		Comuna comuna = new Comuna();
		comuna.setNombre(solicitud.nombre());
		comuna.setRegionId(region.id());
		return aResponse(comunaRepository.save(comuna));
	}

	@Transactional
	public ComunaResponse actualizar(Long id, ComunaRequest solicitud) {
		Comuna comuna = buscar(id);
		RegionResponse region = regionClient.obtener(solicitud.regionId());
		comuna.setNombre(solicitud.nombre());
		comuna.setRegionId(region.id());
		return aResponse(comunaRepository.save(comuna));
	}

	private Comuna buscar(Long id) {
		return comunaRepository.findById(id)
				.orElseThrow(() -> new RecursoNoEncontradoException("La comuna no existe"));
	}

	private ComunaResponse aResponse(Comuna comuna) {
		RegionResponse region = regionClient.obtener(comuna.getRegionId());
		return new ComunaResponse(comuna.getId(), comuna.getNombre(), comuna.getRegionId(),
				region.nombre());
	}
}
