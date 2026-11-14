package cl.duoc.appbelmonte.conductor.client;

import cl.duoc.appbelmonte.conductor.exception.DatosInvalidosException;
import cl.duoc.appbelmonte.conductor.exception.ServicioNoDisponibleException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

@Component
public class VehiculoClient {

	private final RestClient restClient;

	public VehiculoClient(RestClient.Builder restClientBuilder,
			@Value("${servicios.vehiculo.url}") String urlVehiculo) {
		this.restClient = restClientBuilder.baseUrl(urlVehiculo).build();
	}

	public VehiculoResponse obtener(Long id) {
		try {
			VehiculoResponse vehiculo = restClient.get()
					.uri("/api/vehiculos/{id}", id)
					.retrieve()
					.body(VehiculoResponse.class);
			if (vehiculo == null) {
				throw new ServicioNoDisponibleException(
						"El servicio de vehículos no está disponible");
			}
			return vehiculo;
		} catch (HttpClientErrorException.NotFound excepcion) {
			throw new DatosInvalidosException("El vehículo indicado no existe");
		} catch (RestClientException excepcion) {
			throw new ServicioNoDisponibleException("El servicio de vehículos no está disponible");
		}
	}
}
