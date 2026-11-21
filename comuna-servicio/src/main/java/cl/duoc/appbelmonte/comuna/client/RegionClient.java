package cl.duoc.appbelmonte.comuna.client;

import cl.duoc.appbelmonte.comuna.exception.DatosInvalidosException;
import cl.duoc.appbelmonte.comuna.exception.ServicioNoDisponibleException;
import feign.FeignException;
import feign.RetryableException;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "region-servicio")
public interface RegionClient {

	@GetMapping("/api/regiones/{id}")
	RegionResponse obtenerRemoto(@PathVariable("id") Long id);

	default RegionResponse obtener(Long id) {
		try {
			RegionResponse region = obtenerRemoto(id);
			if (region == null) {
				throw new ServicioNoDisponibleException("El servicio de regiones no está disponible");
			}
			return region;
		} catch (FeignException.NotFound excepcion) {
			throw new DatosInvalidosException("La región indicada no existe");
		} catch (RetryableException excepcion) {
			throw new ServicioNoDisponibleException("El servicio de regiones no está disponible");
		} catch (FeignException excepcion) {
			throw new ServicioNoDisponibleException("El servicio de regiones no está disponible");
		}
	}
}
