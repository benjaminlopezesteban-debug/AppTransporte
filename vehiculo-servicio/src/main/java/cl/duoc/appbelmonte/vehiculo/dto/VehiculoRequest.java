package cl.duoc.appbelmonte.vehiculo.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record VehiculoRequest(
		@NotBlank(message = "La patente es obligatoria") String patente,
		Integer anio,
		@NotNull(message = "El modelo es obligatorio") Long modeloId) {
}
