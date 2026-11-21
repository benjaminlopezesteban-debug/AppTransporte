package cl.duoc.appbelmonte.solicitud.client;

import cl.duoc.appbelmonte.solicitud.dto.EstadoSolicitudResponse;
import cl.duoc.appbelmonte.solicitud.exception.DatosInvalidosException;
import cl.duoc.appbelmonte.solicitud.exception.ServicioNoDisponibleException;
import feign.FeignException;
import feign.RetryableException;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "estado-solicitud-servicio")
public interface EstadoSolicitudClient {

	@GetMapping("/api/estados-solicitud/{id}")
	EstadoSolicitudResponse obtenerRemoto(@PathVariable("id") Long id);

	default EstadoSolicitudResponse obtener(Long id) {
		try {
			EstadoSolicitudResponse estado = obtenerRemoto(id);
			if (estado == null) {
				throw new ServicioNoDisponibleException(
						"El servicio de estados de solicitud no está disponible");
			}
			return estado;
		} catch (FeignException.NotFound excepcion) {
			throw new DatosInvalidosException("El estado de solicitud indicado no existe");
		} catch (RetryableException excepcion) {
			throw new ServicioNoDisponibleException(
					"El servicio de estados de solicitud no está disponible");
		} catch (FeignException excepcion) {
			throw new ServicioNoDisponibleException(
					"El servicio de estados de solicitud no está disponible");
		}
	}
}
