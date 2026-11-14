package cl.duoc.appbelmonte.conductor.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record ConductorRequest(
		@NotBlank(message = "El número de licencia es obligatorio") String numeroLicencia,
		@NotNull(message = "El vehículo es obligatorio") Long vehiculoId) {
}
