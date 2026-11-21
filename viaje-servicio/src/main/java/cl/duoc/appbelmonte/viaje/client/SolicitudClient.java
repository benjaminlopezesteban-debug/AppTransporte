package cl.duoc.appbelmonte.viaje.client;

import cl.duoc.appbelmonte.viaje.dto.SolicitudResponse;
import cl.duoc.appbelmonte.viaje.exception.DatosInvalidosException;
import cl.duoc.appbelmonte.viaje.exception.ReglaNegocioException;
import cl.duoc.appbelmonte.viaje.exception.ServicioNoDisponibleException;
import feign.FeignException;
import feign.RetryableException;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(name = "solicitud-servicio")
public interface SolicitudClient {

	@GetMapping("/api/solicitudes/{id}")
	SolicitudResponse obtenerRemoto(@PathVariable("id") Long id);

	@GetMapping("/api/solicitudes")
	SolicitudResponse[] listarPorIdsRemoto(@RequestParam("ids") String ids);

	@PutMapping("/api/solicitudes/{id}/asignar")
	SolicitudResponse asignarRemoto(@PathVariable("id") Long id);

	default SolicitudResponse obtener(Long id) {
		try {
			SolicitudResponse solicitud = obtenerRemoto(id);
			if (solicitud == null) {
				throw new ServicioNoDisponibleException(
						"El servicio de solicitudes no está disponible");
			}
			return solicitud;
		} catch (FeignException.NotFound excepcion) {
			throw new DatosInvalidosException("La solicitud indicada no existe");
		} catch (RetryableException excepcion) {
			throw new ServicioNoDisponibleException(
					"El servicio de solicitudes no está disponible");
		} catch (FeignException excepcion) {
			throw new ServicioNoDisponibleException(
					"El servicio de solicitudes no está disponible");
		}
	}

	default List<SolicitudResponse> listarPorIds(List<Long> ids) {
		String parametroIds = ids.stream().map(String::valueOf).collect(Collectors.joining(","));
		try {
			SolicitudResponse[] solicitudes = listarPorIdsRemoto(parametroIds);
			return solicitudes == null ? List.of() : Arrays.asList(solicitudes);
		} catch (RetryableException excepcion) {
			throw new ServicioNoDisponibleException(
					"El servicio de solicitudes no está disponible");
		} catch (FeignException excepcion) {
			throw new DatosInvalidosException("No se pudieron consultar las solicitudes");
		}
	}

	default SolicitudResponse asignar(Long id) {
		try {
			SolicitudResponse solicitud = asignarRemoto(id);
			if (solicitud == null) {
				throw new ServicioNoDisponibleException(
						"El servicio de solicitudes no está disponible");
			}
			return solicitud;
		} catch (FeignException.Conflict excepcion) {
			throw new ReglaNegocioException("La solicitud ya no está pendiente");
		} catch (FeignException.NotFound excepcion) {
			throw new DatosInvalidosException("La solicitud indicada no existe");
		} catch (RetryableException excepcion) {
			throw new ServicioNoDisponibleException(
					"El servicio de solicitudes no está disponible");
		} catch (FeignException excepcion) {
			throw new DatosInvalidosException("No se pudo asignar la solicitud");
		}
	}
}
