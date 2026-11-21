package cl.duoc.appbelmonte.solicitud.client;

import cl.duoc.appbelmonte.solicitud.dto.PasajeroDatos;
import cl.duoc.appbelmonte.solicitud.dto.PasajeroResponse;
import cl.duoc.appbelmonte.solicitud.exception.DatosInvalidosException;
import cl.duoc.appbelmonte.solicitud.exception.ServicioNoDisponibleException;
import feign.FeignException;
import feign.RetryableException;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "pasajero-servicio")
public interface PasajeroClient {

	@PostMapping("/api/pasajeros")
	PasajeroResponse crearRemoto(@RequestBody PasajeroDatos datos);

	@GetMapping("/api/pasajeros/{id}")
	PasajeroResponse obtenerRemoto(@PathVariable("id") Long id);

	default PasajeroResponse crear(PasajeroDatos datos) {
		try {
			PasajeroResponse pasajero = crearRemoto(datos);
			if (pasajero == null) {
				throw new ServicioNoDisponibleException(
						"El servicio de pasajeros no está disponible");
			}
			return pasajero;
		} catch (RetryableException excepcion) {
			throw new ServicioNoDisponibleException("El servicio de pasajeros no está disponible");
		} catch (FeignException excepcion) {
			throw new DatosInvalidosException("No se pudo crear el pasajero");
		}
	}

	default PasajeroResponse obtener(Long id) {
		try {
			PasajeroResponse pasajero = obtenerRemoto(id);
			if (pasajero == null) {
				throw new ServicioNoDisponibleException(
						"El servicio de pasajeros no está disponible");
			}
			return pasajero;
		} catch (FeignException.NotFound excepcion) {
			throw new DatosInvalidosException("El pasajero indicado no existe");
		} catch (RetryableException excepcion) {
			throw new ServicioNoDisponibleException("El servicio de pasajeros no está disponible");
		} catch (FeignException excepcion) {
			throw new ServicioNoDisponibleException("El servicio de pasajeros no está disponible");
		}
	}
}
