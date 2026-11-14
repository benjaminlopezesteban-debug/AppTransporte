package cl.duoc.appbelmonte.viaje.dto;

import jakarta.validation.constraints.NotNull;

public record ReasignacionRequest(
		@NotNull(message = "El conductor es obligatorio") Long conductorId) {
}
