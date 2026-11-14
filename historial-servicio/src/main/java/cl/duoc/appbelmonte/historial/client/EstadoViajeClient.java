package cl.duoc.appbelmonte.historial.client;

import cl.duoc.appbelmonte.historial.exception.DatosInvalidosException;
import cl.duoc.appbelmonte.historial.exception.ServicioNoDisponibleException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

@Component
public class EstadoViajeClient {

	private final RestClient restClient;

	public EstadoViajeClient(RestClient.Builder restClientBuilder,
			@Value("${servicios.estadoViaje.url}") String urlEstadoViaje) {
		this.restClient = restClientBuilder.baseUrl(urlEstadoViaje).build();
	}

	public EstadoViajeResponse obtener(Long id) {
		try {
			EstadoViajeResponse estado = restClient.get()
					.uri("/api/estados-viaje/{id}", id)
					.retrieve()
					.body(EstadoViajeResponse.class);
			if (estado == null) {
				throw new ServicioNoDisponibleException(
						"El servicio de estados de viaje no está disponible");
			}
			return estado;
		} catch (HttpClientErrorException.NotFound excepcion) {
			throw new DatosInvalidosException("El estado de viaje indicado no existe");
		} catch (RestClientException excepcion) {
			throw new ServicioNoDisponibleException(
					"El servicio de estados de viaje no está disponible");
		}
	}
}
