package cl.duoc.appbelmonte.comuna.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record ComunaRequest(
		@NotBlank(message = "El nombre de la comuna es obligatorio") String nombre,
		@NotNull(message = "La región es obligatoria") Long regionId) {
}
