package cl.duoc.appbelmonte.viaje.client;

import cl.duoc.appbelmonte.viaje.dto.ConductorResponse;
import cl.duoc.appbelmonte.viaje.exception.DatosInvalidosException;
import cl.duoc.appbelmonte.viaje.exception.ServicioNoDisponibleException;
import feign.FeignException;
import feign.RetryableException;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "conductor-servicio")
public interface ConductorClient {

	@GetMapping("/api/conductores/{id}")
	ConductorResponse obtenerRemoto(@PathVariable("id") Long id);

	default ConductorResponse obtener(Long id) {
		try {
			ConductorResponse conductor = obtenerRemoto(id);
			if (conductor == null) {
				throw new ServicioNoDisponibleException(
						"El servicio de conductores no está disponible");
			}
			return conductor;
		} catch (FeignException.NotFound excepcion) {
			throw new DatosInvalidosException("El conductor indicado no existe");
		} catch (RetryableException excepcion) {
			throw new ServicioNoDisponibleException(
					"El servicio de conductores no está disponible");
		} catch (FeignException excepcion) {
			throw new ServicioNoDisponibleException(
					"El servicio de conductores no está disponible");
		}
	}
}
