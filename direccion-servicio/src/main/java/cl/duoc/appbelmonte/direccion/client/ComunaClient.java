package cl.duoc.appbelmonte.direccion.client;

import cl.duoc.appbelmonte.direccion.exception.DatosInvalidosException;
import cl.duoc.appbelmonte.direccion.exception.ServicioNoDisponibleException;
import feign.FeignException;
import feign.RetryableException;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "comuna-servicio")
public interface ComunaClient {

	@GetMapping("/api/comunas/{id}")
	ComunaResponse obtenerRemoto(@PathVariable("id") Long id);

	default ComunaResponse obtener(Long id) {
		try {
			ComunaResponse comuna = obtenerRemoto(id);
			if (comuna == null) {
				throw new ServicioNoDisponibleException("El servicio de comunas no está disponible");
			}
			return comuna;
		} catch (FeignException.NotFound excepcion) {
			throw new DatosInvalidosException("La comuna indicada no existe");
		} catch (RetryableException excepcion) {
			throw new ServicioNoDisponibleException("El servicio de comunas no está disponible");
		} catch (FeignException excepcion) {
			throw new ServicioNoDisponibleException("El servicio de comunas no está disponible");
		}
	}
}
