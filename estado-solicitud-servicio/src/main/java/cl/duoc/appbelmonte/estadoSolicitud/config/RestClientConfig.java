package cl.duoc.appbelmonte.estadoSolicitud.config;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;
import org.springframework.http.HttpHeaders;
import org.springframework.web.client.RestClient;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

@Configuration
public class RestClientConfig {

	@Bean
	@Scope("prototype")
	public RestClient.Builder restClientBuilder() {
		return RestClient.builder().requestInterceptor((peticion, cuerpo, ejecucion) -> {
			ServletRequestAttributes atributos =
					(ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
			if (atributos != null) {
				HttpServletRequest solicitud = atributos.getRequest();
				String autorizacion = solicitud.getHeader(HttpHeaders.AUTHORIZATION);
				if (autorizacion != null && !autorizacion.isBlank()) {
					peticion.getHeaders().set(HttpHeaders.AUTHORIZATION, autorizacion);
				}
			}
			return ejecucion.execute(peticion, cuerpo);
		});
	}
}
