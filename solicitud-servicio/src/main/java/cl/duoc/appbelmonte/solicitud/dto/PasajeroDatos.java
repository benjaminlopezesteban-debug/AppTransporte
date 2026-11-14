package cl.duoc.appbelmonte.solicitud.dto;

import jakarta.validation.constraints.NotBlank;

public record PasajeroDatos(
		@NotBlank(message = "El nombre del pasajero es obligatorio") String nombre,
		String telefono) {
}
