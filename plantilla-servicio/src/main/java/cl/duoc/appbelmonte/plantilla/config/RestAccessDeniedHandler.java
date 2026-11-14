package cl.duoc.appbelmonte.plantilla.config;

import cl.duoc.appbelmonte.plantilla.dto.ErrorResponse;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;
import tools.jackson.databind.ObjectMapper;

@Component
public class RestAccessDeniedHandler implements AccessDeniedHandler {

	private final ObjectMapper objectMapper;

	public RestAccessDeniedHandler(ObjectMapper objectMapper) {
		this.objectMapper = objectMapper;
	}

	@Override
	public void handle(HttpServletRequest solicitud, HttpServletResponse respuesta,
			AccessDeniedException excepcion) throws IOException, ServletException {
		respuesta.setStatus(HttpStatus.FORBIDDEN.value());
		respuesta.setContentType(MediaType.APPLICATION_JSON_VALUE);
		respuesta.setCharacterEncoding("UTF-8");
		objectMapper.writeValue(respuesta.getOutputStream(),
				ErrorResponse.de(HttpStatus.FORBIDDEN, "No tiene permisos para realizar esta acción"));
	}
}
