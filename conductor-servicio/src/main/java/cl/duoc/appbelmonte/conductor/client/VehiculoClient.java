package cl.duoc.appbelmonte.conductor.client;

import cl.duoc.appbelmonte.conductor.exception.DatosInvalidosException;
import cl.duoc.appbelmonte.conductor.exception.ServicioNoDisponibleException;
import feign.FeignException;
import feign.RetryableException;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "vehiculo-servicio")
public interface VehiculoClient {

	@GetMapping("/api/vehiculos/{id}")
	VehiculoResponse obtenerRemoto(@PathVariable("id") Long id);

	default VehiculoResponse obtener(Long id) {
		try {
			VehiculoResponse vehiculo = obtenerRemoto(id);
			if (vehiculo == null) {
				throw new ServicioNoDisponibleException(
						"El servicio de vehículos no está disponible");
			}
			return vehiculo;
		} catch (FeignException.NotFound excepcion) {
			throw new DatosInvalidosException("El vehículo indicado no existe");
		} catch (RetryableException excepcion) {
			throw new ServicioNoDisponibleException("El servicio de vehículos no está disponible");
		} catch (FeignException excepcion) {
			throw new ServicioNoDisponibleException("El servicio de vehículos no está disponible");
		}
	}
}
