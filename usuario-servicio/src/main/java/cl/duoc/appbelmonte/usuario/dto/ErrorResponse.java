package cl.duoc.appbelmonte.usuario.dto;

import java.time.LocalDateTime;
import org.springframework.http.HttpStatus;

public record ErrorResponse(int estado, String mensaje, LocalDateTime fecha) {

	public static ErrorResponse de(HttpStatus estado, String mensaje) {
		return new ErrorResponse(estado.value(), mensaje, LocalDateTime.now().withNano(0));
	}
}
