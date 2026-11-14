package cl.duoc.appbelmonte.pasajero.service;

import cl.duoc.appbelmonte.pasajero.dto.PasajeroRequest;
import cl.duoc.appbelmonte.pasajero.dto.PasajeroResponse;
import cl.duoc.appbelmonte.pasajero.exception.RecursoNoEncontradoException;
import cl.duoc.appbelmonte.pasajero.model.Pasajero;
import cl.duoc.appbelmonte.pasajero.repository.PasajeroRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PasajeroService {

	private final PasajeroRepository pasajeroRepository;

	public PasajeroService(PasajeroRepository pasajeroRepository) {
		this.pasajeroRepository = pasajeroRepository;
	}

	@Transactional
	public PasajeroResponse crear(PasajeroRequest solicitud) {
		Pasajero pasajero = new Pasajero();
		pasajero.setNombre(solicitud.nombre());
		pasajero.setTelefono(solicitud.telefono());
		return aResponse(pasajeroRepository.save(pasajero));
	}

	@Transactional(readOnly = true)
	public PasajeroResponse obtener(Long id) {
		Pasajero pasajero = pasajeroRepository.findById(id)
				.orElseThrow(() -> new RecursoNoEncontradoException("El pasajero no existe"));
		return aResponse(pasajero);
	}

	private PasajeroResponse aResponse(Pasajero pasajero) {
		return new PasajeroResponse(pasajero.getId(), pasajero.getNombre(), pasajero.getTelefono());
	}
}
