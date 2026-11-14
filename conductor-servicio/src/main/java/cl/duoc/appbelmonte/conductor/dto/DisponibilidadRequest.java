package cl.duoc.appbelmonte.conductor.dto;

import jakarta.validation.constraints.NotNull;

public record DisponibilidadRequest(
		@NotNull(message = "La disponibilidad es obligatoria") Boolean disponible) {
}
