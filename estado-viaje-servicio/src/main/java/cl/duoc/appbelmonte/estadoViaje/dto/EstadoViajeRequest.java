package cl.duoc.appbelmonte.estadoViaje.dto;

import jakarta.validation.constraints.NotBlank;

public record EstadoViajeRequest(
		@NotBlank(message = "El nombre del estado es obligatorio") String nombre) {
}
