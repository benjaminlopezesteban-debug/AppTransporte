package cl.duoc.appbelmonte.solicitud.client;

import cl.duoc.appbelmonte.solicitud.dto.PasajeroDatos;
import cl.duoc.appbelmonte.solicitud.dto.PasajeroResponse;
import cl.duoc.appbelmonte.solicitud.exception.DatosInvalidosException;
import cl.duoc.appbelmonte.solicitud.exception.ServicioNoDisponibleException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

@Component
public class PasajeroClient {

	private final RestClient restClient;

	public PasajeroClient(RestClient.Builder restClientBuilder,
			@Value("${servicios.pasajero.url}") String urlPasajero) {
		this.restClient = restClientBuilder.baseUrl(urlPasajero).build();
	}

	public PasajeroResponse crear(PasajeroDatos datos) {
		try {
			PasajeroResponse pasajero = restClient.post()
					.uri("/api/pasajeros")
					.body(datos)
					.retrieve()
					.body(PasajeroResponse.class);
			if (pasajero == null) {
				throw new ServicioNoDisponibleException(
						"El servicio de pasajeros no está disponible");
			}
			return pasajero;
		} catch (HttpClientErrorException excepcion) {
			throw new DatosInvalidosException("No se pudo crear el pasajero");
		} catch (RestClientException excepcion) {
			throw new ServicioNoDisponibleException("El servicio de pasajeros no está disponible");
		}
	}

	public PasajeroResponse obtener(Long id) {
		try {
			PasajeroResponse pasajero = restClient.get()
					.uri("/api/pasajeros/{id}", id)
					.retrieve()
					.body(PasajeroResponse.class);
			if (pasajero == null) {
				throw new ServicioNoDisponibleException(
						"El servicio de pasajeros no está disponible");
			}
			return pasajero;
		} catch (HttpClientErrorException.NotFound excepcion) {
			throw new DatosInvalidosException("El pasajero indicado no existe");
		} catch (RestClientException excepcion) {
			throw new ServicioNoDisponibleException("El servicio de pasajeros no está disponible");
		}
	}
}
