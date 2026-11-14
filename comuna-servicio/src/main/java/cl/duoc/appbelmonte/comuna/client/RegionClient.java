package cl.duoc.appbelmonte.comuna.client;

import cl.duoc.appbelmonte.comuna.exception.DatosInvalidosException;
import cl.duoc.appbelmonte.comuna.exception.ServicioNoDisponibleException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

@Component
public class RegionClient {

	private final RestClient restClient;

	public RegionClient(RestClient.Builder restClientBuilder,
			@Value("${servicios.region.url}") String urlRegion) {
		this.restClient = restClientBuilder.baseUrl(urlRegion).build();
	}

	public RegionResponse obtener(Long id) {
		try {
			RegionResponse region = restClient.get()
					.uri("/api/regiones/{id}", id)
					.retrieve()
					.body(RegionResponse.class);
			if (region == null) {
				throw new ServicioNoDisponibleException("El servicio de regiones no está disponible");
			}
			return region;
		} catch (HttpClientErrorException.NotFound excepcion) {
			throw new DatosInvalidosException("La región indicada no existe");
		} catch (RestClientException excepcion) {
			throw new ServicioNoDisponibleException("El servicio de regiones no está disponible");
		}
	}
}
