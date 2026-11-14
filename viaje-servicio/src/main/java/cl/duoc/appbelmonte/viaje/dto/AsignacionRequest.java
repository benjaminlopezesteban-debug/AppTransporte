package cl.duoc.appbelmonte.viaje.dto;

import jakarta.validation.constraints.NotNull;

public record AsignacionRequest(
		@NotNull(message = "La solicitud es obligatoria") Long solicitudId,
		@NotNull(message = "El conductor es obligatorio") Long conductorId) {
}
