package cl.duoc.appbelmonte.historial.dto;

import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;

public record HistorialRequest(
		LocalDateTime fechaHora,
		String detalle,
		@NotNull(message = "El viaje es obligatorio") Long viajeId,
		Long estadoAnteriorId,
		@NotNull(message = "El estado nuevo es obligatorio") Long estadoNuevoId) {
}
