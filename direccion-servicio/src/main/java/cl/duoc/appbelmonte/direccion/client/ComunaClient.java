package cl.duoc.appbelmonte.direccion.client;

import cl.duoc.appbelmonte.direccion.exception.DatosInvalidosException;
import cl.duoc.appbelmonte.direccion.exception.ServicioNoDisponibleException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

@Component
public class ComunaClient {

	private final RestClient restClient;

	public ComunaClient(RestClient.Builder restClientBuilder,
			@Value("${servicios.comuna.url}") String urlComuna) {
		this.restClient = restClientBuilder.baseUrl(urlComuna).build();
	}

	public ComunaResponse obtener(Long id) {
		try {
			ComunaResponse comuna = restClient.get()
					.uri("/api/comunas/{id}", id)
					.retrieve()
					.body(ComunaResponse.class);
			if (comuna == null) {
				throw new ServicioNoDisponibleException("El servicio de comunas no está disponible");
			}
			return comuna;
		} catch (HttpClientErrorException.NotFound excepcion) {
			throw new DatosInvalidosException("La comuna indicada no existe");
		} catch (RestClientException excepcion) {
			throw new ServicioNoDisponibleException("El servicio de comunas no está disponible");
		}
	}
}
