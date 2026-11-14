package cl.duoc.appbelmonte.rol.dto;

import jakarta.validation.constraints.NotBlank;

public record RolRequest(
		@NotBlank(message = "El nombre del rol es obligatorio") String nombre) {
}
