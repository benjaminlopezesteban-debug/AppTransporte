package cl.duoc.appbelmonte.solicitud.client;

import cl.duoc.appbelmonte.solicitud.dto.EstadoSolicitudResponse;
import cl.duoc.appbelmonte.solicitud.exception.DatosInvalidosException;
import cl.duoc.appbelmonte.solicitud.exception.ServicioNoDisponibleException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

@Component
public class EstadoSolicitudClient {

	private final RestClient restClient;

	public EstadoSolicitudClient(RestClient.Builder restClientBuilder,
			@Value("${servicios.estadoSolicitud.url}") String urlEstadoSolicitud) {
		this.restClient = restClientBuilder.baseUrl(urlEstadoSolicitud).build();
	}

	public EstadoSolicitudResponse obtener(Long id) {
		try {
			EstadoSolicitudResponse estado = restClient.get()
					.uri("/api/estados-solicitud/{id}", id)
					.retrieve()
					.body(EstadoSolicitudResponse.class);
			if (estado == null) {
				throw new ServicioNoDisponibleException(
						"El servicio de estados de solicitud no está disponible");
			}
			return estado;
		} catch (HttpClientErrorException.NotFound excepcion) {
			throw new DatosInvalidosException("El estado de solicitud indicado no existe");
		} catch (RestClientException excepcion) {
			throw new ServicioNoDisponibleException(
					"El servicio de estados de solicitud no está disponible");
		}
	}
}
