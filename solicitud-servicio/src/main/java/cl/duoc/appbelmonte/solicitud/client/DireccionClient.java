package cl.duoc.appbelmonte.solicitud.client;

import cl.duoc.appbelmonte.solicitud.dto.DireccionDatos;
import cl.duoc.appbelmonte.solicitud.dto.DireccionResponse;
import cl.duoc.appbelmonte.solicitud.exception.DatosInvalidosException;
import cl.duoc.appbelmonte.solicitud.exception.ServicioNoDisponibleException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

@Component
public class DireccionClient {

	private final RestClient restClient;

	public DireccionClient(RestClient.Builder restClientBuilder,
			@Value("${servicios.direccion.url}") String urlDireccion) {
		this.restClient = restClientBuilder.baseUrl(urlDireccion).build();
	}

	public DireccionResponse crear(DireccionDatos datos) {
		try {
			DireccionResponse direccion = restClient.post()
					.uri("/api/direcciones")
					.body(datos)
					.retrieve()
					.body(DireccionResponse.class);
			if (direccion == null) {
				throw new ServicioNoDisponibleException(
						"El servicio de direcciones no está disponible");
			}
			return direccion;
		} catch (HttpClientErrorException excepcion) {
			throw new DatosInvalidosException("No se pudo crear la dirección");
		} catch (RestClientException excepcion) {
			throw new ServicioNoDisponibleException(
					"El servicio de direcciones no está disponible");
		}
	}

	public DireccionResponse obtener(Long id) {
		try {
			DireccionResponse direccion = restClient.get()
					.uri("/api/direcciones/{id}", id)
					.retrieve()
					.body(DireccionResponse.class);
			if (direccion == null) {
				throw new ServicioNoDisponibleException(
						"El servicio de direcciones no está disponible");
			}
			return direccion;
		} catch (HttpClientErrorException.NotFound excepcion) {
			throw new DatosInvalidosException("La dirección indicada no existe");
		} catch (RestClientException excepcion) {
			throw new ServicioNoDisponibleException(
					"El servicio de direcciones no está disponible");
		}
	}
}
