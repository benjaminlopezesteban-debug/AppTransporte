package cl.duoc.appbelmonte.modeloVehiculo.client;

import cl.duoc.appbelmonte.modeloVehiculo.exception.DatosInvalidosException;
import cl.duoc.appbelmonte.modeloVehiculo.exception.ServicioNoDisponibleException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

@Component
public class MarcaClient {

	private final RestClient restClient;

	public MarcaClient(RestClient.Builder restClientBuilder,
			@Value("${servicios.marca.url}") String urlMarca) {
		this.restClient = restClientBuilder.baseUrl(urlMarca).build();
	}

	public MarcaResponse obtener(Long id) {
		try {
			MarcaResponse marca = restClient.get()
					.uri("/api/marcas/{id}", id)
					.retrieve()
					.body(MarcaResponse.class);
			if (marca == null) {
				throw new ServicioNoDisponibleException("El servicio de marcas no está disponible");
			}
			return marca;
		} catch (HttpClientErrorException.NotFound excepcion) {
			throw new DatosInvalidosException("La marca indicada no existe");
		} catch (RestClientException excepcion) {
			throw new ServicioNoDisponibleException("El servicio de marcas no está disponible");
		}
	}
}
