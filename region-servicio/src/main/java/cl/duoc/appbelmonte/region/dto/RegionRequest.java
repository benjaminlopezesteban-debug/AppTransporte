package cl.duoc.appbelmonte.region.dto;

import jakarta.validation.constraints.NotBlank;

public record RegionRequest(
		@NotBlank(message = "El nombre de la región es obligatorio") String nombre) {
}
