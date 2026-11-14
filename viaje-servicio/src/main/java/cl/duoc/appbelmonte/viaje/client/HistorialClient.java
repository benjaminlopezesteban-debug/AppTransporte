package cl.duoc.appbelmonte.viaje.client;

import cl.duoc.appbelmonte.viaje.dto.HistorialRequest;
import cl.duoc.appbelmonte.viaje.exception.DatosInvalidosException;
import cl.duoc.appbelmonte.viaje.exception.ServicioNoDisponibleException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

@Component
public class HistorialClient {

	private final RestClient restClient;

	public HistorialClient(RestClient.Builder restClientBuilder,
			@Value("${servicios.historial.url}") String urlHistorial) {
		this.restClient = restClientBuilder.baseUrl(urlHistorial).build();
	}

	public void registrar(HistorialRequest solicitud) {
		try {
			restClient.post()
					.uri("/api/historial")
					.body(solicitud)
					.retrieve()
					.toBodilessEntity();
		} catch (HttpClientErrorException excepcion) {
			throw new DatosInvalidosException("No se pudo registrar el historial del viaje");
		} catch (RestClientException excepcion) {
			throw new ServicioNoDisponibleException(
					"El servicio de historial no está disponible");
		}
	}
}
