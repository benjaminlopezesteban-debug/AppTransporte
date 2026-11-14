package cl.duoc.appbelmonte.viaje.client;

import cl.duoc.appbelmonte.viaje.dto.SolicitudResponse;
import cl.duoc.appbelmonte.viaje.exception.DatosInvalidosException;
import cl.duoc.appbelmonte.viaje.exception.ReglaNegocioException;
import cl.duoc.appbelmonte.viaje.exception.ServicioNoDisponibleException;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

@Component
public class SolicitudClient {

	private final RestClient restClient;

	public SolicitudClient(RestClient.Builder restClientBuilder,
			@Value("${servicios.solicitud.url}") String urlSolicitud) {
		this.restClient = restClientBuilder.baseUrl(urlSolicitud).build();
	}

	public SolicitudResponse obtener(Long id) {
		try {
			SolicitudResponse solicitud = restClient.get()
					.uri("/api/solicitudes/{id}", id)
					.retrieve()
					.body(SolicitudResponse.class);
			if (solicitud == null) {
				throw new ServicioNoDisponibleException(
						"El servicio de solicitudes no está disponible");
			}
			return solicitud;
		} catch (HttpClientErrorException.Forbidden excepcion) {
			throw new AccessDeniedException(
					"No tiene permisos para consultar la solicitud");
		} catch (HttpClientErrorException.NotFound excepcion) {
			throw new DatosInvalidosException("La solicitud indicada no existe");
		} catch (RestClientException excepcion) {
			throw new ServicioNoDisponibleException(
					"El servicio de solicitudes no está disponible");
		}
	}

	public List<SolicitudResponse> listarPorIds(List<Long> ids) {
		String parametroIds = ids.stream().map(String::valueOf).collect(Collectors.joining(","));
		try {
			SolicitudResponse[] solicitudes = restClient.get()
					.uri(constructor -> constructor.path("/api/solicitudes")
							.queryParam("ids", parametroIds)
							.build())
					.retrieve()
					.body(SolicitudResponse[].class);
			return solicitudes == null ? List.of() : Arrays.asList(solicitudes);
		} catch (HttpClientErrorException excepcion) {
			throw new DatosInvalidosException("No se pudieron consultar las solicitudes");
		} catch (RestClientException excepcion) {
			throw new ServicioNoDisponibleException(
					"El servicio de solicitudes no está disponible");
		}
	}

	public SolicitudResponse asignar(Long id) {
		try {
			SolicitudResponse solicitud = restClient.put()
					.uri("/api/solicitudes/{id}/asignar", id)
					.retrieve()
					.body(SolicitudResponse.class);
			if (solicitud == null) {
				throw new ServicioNoDisponibleException(
						"El servicio de solicitudes no está disponible");
			}
			return solicitud;
		} catch (HttpClientErrorException.Forbidden excepcion) {
			throw new AccessDeniedException(
					"No tiene permisos para asignar la solicitud");
		} catch (HttpClientErrorException.Conflict excepcion) {
			throw new ReglaNegocioException("La solicitud ya no está pendiente");
		} catch (HttpClientErrorException.NotFound excepcion) {
			throw new DatosInvalidosException("La solicitud indicada no existe");
		} catch (HttpClientErrorException excepcion) {
			throw new DatosInvalidosException("No se pudo asignar la solicitud");
		} catch (RestClientException excepcion) {
			throw new ServicioNoDisponibleException(
					"El servicio de solicitudes no está disponible");
		}
	}
}
