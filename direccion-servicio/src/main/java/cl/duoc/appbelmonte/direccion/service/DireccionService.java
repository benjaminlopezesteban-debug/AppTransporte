package cl.duoc.appbelmonte.direccion.service;

import cl.duoc.appbelmonte.direccion.client.ComunaClient;
import cl.duoc.appbelmonte.direccion.client.ComunaResponse;
import cl.duoc.appbelmonte.direccion.dto.DireccionRequest;
import cl.duoc.appbelmonte.direccion.dto.DireccionResponse;
import cl.duoc.appbelmonte.direccion.exception.RecursoNoEncontradoException;
import cl.duoc.appbelmonte.direccion.model.Direccion;
import cl.duoc.appbelmonte.direccion.repository.DireccionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class DireccionService {

	private final DireccionRepository direccionRepository;
	private final ComunaClient comunaClient;

	public DireccionService(DireccionRepository direccionRepository, ComunaClient comunaClient) {
		this.direccionRepository = direccionRepository;
		this.comunaClient = comunaClient;
	}

	@Transactional
	public DireccionResponse crear(DireccionRequest solicitud) {
		ComunaResponse comuna = comunaClient.obtener(solicitud.comunaId());
		Direccion direccion = new Direccion();
		direccion.setCalle(solicitud.calle());
		direccion.setLatitud(solicitud.latitud());
		direccion.setLongitud(solicitud.longitud());
		direccion.setComunaId(comuna.id());
		return aResponse(direccionRepository.save(direccion));
	}

	@Transactional(readOnly = true)
	public DireccionResponse obtener(Long id) {
		Direccion direccion = direccionRepository.findById(id)
				.orElseThrow(() -> new RecursoNoEncontradoException("La dirección no existe"));
		return aResponse(direccion);
	}

	private DireccionResponse aResponse(Direccion direccion) {
		ComunaResponse comuna = comunaClient.obtener(direccion.getComunaId());
		return new DireccionResponse(direccion.getId(), direccion.getCalle(),
				direccion.getLatitud(), direccion.getLongitud(), direccion.getComunaId(),
				comuna.nombre(), comuna.regionId(), comuna.regionNombre());
	}
}
