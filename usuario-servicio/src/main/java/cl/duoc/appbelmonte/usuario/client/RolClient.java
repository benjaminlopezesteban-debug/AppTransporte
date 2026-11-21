package cl.duoc.appbelmonte.usuario.client;

import cl.duoc.appbelmonte.usuario.exception.DatosInvalidosException;
import cl.duoc.appbelmonte.usuario.exception.ServicioNoDisponibleException;
import feign.FeignException;
import feign.RetryableException;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "rol-servicio")
public interface RolClient {

	@GetMapping("/api/roles/{id}")
	RolResponse obtenerRemoto(@PathVariable("id") Long id);

	default RolResponse obtener(Long id) {
		try {
			RolResponse rol = obtenerRemoto(id);
			if (rol == null) {
				throw new ServicioNoDisponibleException("El servicio de roles no está disponible");
			}
			return rol;
		} catch (FeignException.NotFound excepcion) {
			throw new DatosInvalidosException("El rol indicado no existe");
		} catch (RetryableException excepcion) {
			throw new ServicioNoDisponibleException("El servicio de roles no está disponible");
		} catch (FeignException excepcion) {
			throw new ServicioNoDisponibleException("El servicio de roles no está disponible");
		}
	}
}
