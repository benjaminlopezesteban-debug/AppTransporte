package cl.duoc.appbelmonte.vehiculo.client;

import cl.duoc.appbelmonte.vehiculo.exception.DatosInvalidosException;
import cl.duoc.appbelmonte.vehiculo.exception.ServicioNoDisponibleException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

@Component
public class ModeloVehiculoClient {

	private final RestClient restClient;

	public ModeloVehiculoClient(RestClient.Builder restClientBuilder,
			@Value("${servicios.modeloVehiculo.url}") String urlModelo) {
		this.restClient = restClientBuilder.baseUrl(urlModelo).build();
	}

	public ModeloVehiculoResponse obtener(Long id) {
		try {
			ModeloVehiculoResponse modelo = restClient.get()
					.uri("/api/modelos/{id}", id)
					.retrieve()
					.body(ModeloVehiculoResponse.class);
			if (modelo == null) {
				throw new ServicioNoDisponibleException(
						"El servicio de modelos no está disponible");
			}
			return modelo;
		} catch (HttpClientErrorException.NotFound excepcion) {
			throw new DatosInvalidosException("El modelo indicado no existe");
		} catch (RestClientException excepcion) {
			throw new ServicioNoDisponibleException("El servicio de modelos no está disponible");
		}
	}
}
