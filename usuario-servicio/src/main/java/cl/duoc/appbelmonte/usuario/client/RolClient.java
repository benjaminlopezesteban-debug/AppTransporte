package cl.duoc.appbelmonte.usuario.client;

import cl.duoc.appbelmonte.usuario.exception.DatosInvalidosException;
import cl.duoc.appbelmonte.usuario.exception.ServicioNoDisponibleException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

@Component
public class RolClient {

	private final RestClient restClient;

	public RolClient(RestClient.Builder restClientBuilder,
			@Value("${servicios.rol.url}") String urlRol) {
		this.restClient = restClientBuilder.baseUrl(urlRol).build();
	}

	public RolResponse obtener(Long id) {
		try {
			RolResponse rol = restClient.get()
					.uri("/api/roles/{id}", id)
					.retrieve()
					.body(RolResponse.class);
			if (rol == null) {
				throw new ServicioNoDisponibleException("El servicio de roles no está disponible");
			}
			return rol;
		} catch (HttpClientErrorException.NotFound excepcion) {
			throw new DatosInvalidosException("El rol indicado no existe");
		} catch (RestClientException excepcion) {
			throw new ServicioNoDisponibleException("El servicio de roles no está disponible");
		}
	}
}
