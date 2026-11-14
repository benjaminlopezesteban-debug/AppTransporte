package cl.duoc.appbelmonte.pasajero.dto;

import jakarta.validation.constraints.NotBlank;

public record PasajeroRequest(
		@NotBlank(message = "El nombre del pasajero es obligatorio") String nombre,
		String telefono) {
}
