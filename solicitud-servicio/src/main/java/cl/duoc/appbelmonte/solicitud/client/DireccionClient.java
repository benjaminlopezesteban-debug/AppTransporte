package cl.duoc.appbelmonte.solicitud.client;

import cl.duoc.appbelmonte.solicitud.dto.DireccionDatos;
import cl.duoc.appbelmonte.solicitud.dto.DireccionResponse;
import cl.duoc.appbelmonte.solicitud.exception.DatosInvalidosException;
import cl.duoc.appbelmonte.solicitud.exception.ServicioNoDisponibleException;
import feign.FeignException;
import feign.RetryableException;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "direccion-servicio")
public interface DireccionClient {

	@PostMapping("/api/direcciones")
	DireccionResponse crearRemoto(@RequestBody DireccionDatos datos);

	@GetMapping("/api/direcciones/{id}")
	DireccionResponse obtenerRemoto(@PathVariable("id") Long id);

	default DireccionResponse crear(DireccionDatos datos) {
		try {
			DireccionResponse direccion = crearRemoto(datos);
			if (direccion == null) {
				throw new ServicioNoDisponibleException(
						"El servicio de direcciones no está disponible");
			}
			return direccion;
		} catch (RetryableException excepcion) {
			throw new ServicioNoDisponibleException(
					"El servicio de direcciones no está disponible");
		} catch (FeignException excepcion) {
			throw new DatosInvalidosException("No se pudo crear la dirección");
		}
	}

	default DireccionResponse obtener(Long id) {
		try {
			DireccionResponse direccion = obtenerRemoto(id);
			if (direccion == null) {
				throw new ServicioNoDisponibleException(
						"El servicio de direcciones no está disponible");
			}
			return direccion;
		} catch (FeignException.NotFound excepcion) {
			throw new DatosInvalidosException("La dirección indicada no existe");
		} catch (RetryableException excepcion) {
			throw new ServicioNoDisponibleException(
					"El servicio de direcciones no está disponible");
		} catch (FeignException excepcion) {
			throw new ServicioNoDisponibleException(
					"El servicio de direcciones no está disponible");
		}
	}
}
