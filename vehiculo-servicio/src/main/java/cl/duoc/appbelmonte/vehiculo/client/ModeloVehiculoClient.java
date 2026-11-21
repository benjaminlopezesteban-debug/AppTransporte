package cl.duoc.appbelmonte.vehiculo.client;

import cl.duoc.appbelmonte.vehiculo.exception.DatosInvalidosException;
import cl.duoc.appbelmonte.vehiculo.exception.ServicioNoDisponibleException;
import feign.FeignException;
import feign.RetryableException;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "modelo-vehiculo-servicio")
public interface ModeloVehiculoClient {

	@GetMapping("/api/modelos/{id}")
	ModeloVehiculoResponse obtenerRemoto(@PathVariable("id") Long id);

	default ModeloVehiculoResponse obtener(Long id) {
		try {
			ModeloVehiculoResponse modelo = obtenerRemoto(id);
			if (modelo == null) {
				throw new ServicioNoDisponibleException(
						"El servicio de modelos no está disponible");
			}
			return modelo;
		} catch (FeignException.NotFound excepcion) {
			throw new DatosInvalidosException("El modelo indicado no existe");
		} catch (RetryableException excepcion) {
			throw new ServicioNoDisponibleException("El servicio de modelos no está disponible");
		} catch (FeignException excepcion) {
			throw new ServicioNoDisponibleException("El servicio de modelos no está disponible");
		}
	}
}
