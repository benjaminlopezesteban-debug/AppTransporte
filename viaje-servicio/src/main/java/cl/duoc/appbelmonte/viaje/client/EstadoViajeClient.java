package cl.duoc.appbelmonte.viaje.client;

import cl.duoc.appbelmonte.viaje.dto.EstadoViajeResponse;
import cl.duoc.appbelmonte.viaje.exception.DatosInvalidosException;
import cl.duoc.appbelmonte.viaje.exception.ServicioNoDisponibleException;
import feign.FeignException;
import feign.RetryableException;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "estado-viaje-servicio")
public interface EstadoViajeClient {

	@GetMapping("/api/estados-viaje/{id}")
	EstadoViajeResponse obtenerRemoto(@PathVariable("id") Long id);

	default EstadoViajeResponse obtener(Long id) {
		try {
			EstadoViajeResponse estado = obtenerRemoto(id);
			if (estado == null) {
				throw new ServicioNoDisponibleException(
						"El servicio de estados de viaje no está disponible");
			}
			return estado;
		} catch (FeignException.NotFound excepcion) {
			throw new DatosInvalidosException("El estado de viaje indicado no existe");
		} catch (RetryableException excepcion) {
			throw new ServicioNoDisponibleException(
					"El servicio de estados de viaje no está disponible");
		} catch (FeignException excepcion) {
			throw new ServicioNoDisponibleException(
					"El servicio de estados de viaje no está disponible");
		}
	}
}
