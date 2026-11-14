package cl.duoc.appbelmonte.modeloVehiculo.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record ModeloVehiculoRequest(
		@NotBlank(message = "El nombre del modelo es obligatorio") String nombre,
		@NotNull(message = "La marca es obligatoria") Long marcaId) {
}
