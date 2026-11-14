package cl.duoc.appbelmonte.viaje.client;

import cl.duoc.appbelmonte.viaje.dto.ConductorResponse;
import cl.duoc.appbelmonte.viaje.exception.DatosInvalidosException;
import cl.duoc.appbelmonte.viaje.exception.ServicioNoDisponibleException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

@Component
public class ConductorClient {

	private final RestClient restClient;

	public ConductorClient(RestClient.Builder restClientBuilder,
			@Value("${servicios.conductor.url}") String urlConductor) {
		this.restClient = restClientBuilder.baseUrl(urlConductor).build();
	}

	public ConductorResponse obtener(Long id) {
		try {
			ConductorResponse conductor = restClient.get()
					.uri("/api/conductores/{id}", id)
					.retrieve()
					.body(ConductorResponse.class);
			if (conductor == null) {
				throw new ServicioNoDisponibleException(
						"El servicio de conductores no está disponible");
			}
			return conductor;
		} catch (HttpClientErrorException.NotFound excepcion) {
			throw new DatosInvalidosException("El conductor indicado no existe");
		} catch (RestClientException excepcion) {
			throw new ServicioNoDisponibleException(
					"El servicio de conductores no está disponible");
		}
	}
}
