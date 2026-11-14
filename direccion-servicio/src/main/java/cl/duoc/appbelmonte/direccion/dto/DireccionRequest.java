package cl.duoc.appbelmonte.direccion.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record DireccionRequest(
		@NotBlank(message = "La calle es obligatoria") String calle,
		@NotNull(message = "La latitud es obligatoria") Double latitud,
		@NotNull(message = "La longitud es obligatoria") Double longitud,
		@NotNull(message = "La comuna es obligatoria") Long comunaId) {
}
