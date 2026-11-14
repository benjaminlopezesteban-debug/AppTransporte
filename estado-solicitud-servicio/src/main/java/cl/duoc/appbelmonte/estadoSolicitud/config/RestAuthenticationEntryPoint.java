package cl.duoc.appbelmonte.estadoSolicitud.config;

import cl.duoc.appbelmonte.estadoSolicitud.dto.ErrorResponse;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;
import tools.jackson.databind.ObjectMapper;

@Component
public class RestAuthenticationEntryPoint implements AuthenticationEntryPoint {

	private final ObjectMapper objectMapper;

	public RestAuthenticationEntryPoint(ObjectMapper objectMapper) {
		this.objectMapper = objectMapper;
	}

	@Override
	public void commence(HttpServletRequest solicitud, HttpServletResponse respuesta,
			AuthenticationException excepcion) throws IOException, ServletException {
		respuesta.setStatus(HttpStatus.UNAUTHORIZED.value());
		respuesta.setContentType(MediaType.APPLICATION_JSON_VALUE);
		respuesta.setCharacterEncoding("UTF-8");
		objectMapper.writeValue(respuesta.getOutputStream(),
				ErrorResponse.de(HttpStatus.UNAUTHORIZED, "Debe iniciar sesión para acceder a este recurso"));
	}
}
