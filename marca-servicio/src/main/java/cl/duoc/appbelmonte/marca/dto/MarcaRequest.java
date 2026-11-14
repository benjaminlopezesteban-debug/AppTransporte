package cl.duoc.appbelmonte.marca.dto;

import jakarta.validation.constraints.NotBlank;

public record MarcaRequest(
		@NotBlank(message = "El nombre de la marca es obligatorio") String nombre) {
}
