package cl.duoc.appbelmonte.viaje.client;

import cl.duoc.appbelmonte.viaje.dto.HistorialRequest;
import cl.duoc.appbelmonte.viaje.exception.DatosInvalidosException;
import cl.duoc.appbelmonte.viaje.exception.ServicioNoDisponibleException;
import feign.FeignException;
import feign.RetryableException;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "historial-servicio")
public interface HistorialClient {

	@PostMapping("/api/historial")
	void registrarRemoto(@RequestBody HistorialRequest solicitud);

	default void registrar(HistorialRequest solicitud) {
		try {
			registrarRemoto(solicitud);
		} catch (RetryableException excepcion) {
			throw new ServicioNoDisponibleException(
					"El servicio de historial no está disponible");
		} catch (FeignException excepcion) {
			throw new DatosInvalidosException("No se pudo registrar el historial del viaje");
		}
	}
}
