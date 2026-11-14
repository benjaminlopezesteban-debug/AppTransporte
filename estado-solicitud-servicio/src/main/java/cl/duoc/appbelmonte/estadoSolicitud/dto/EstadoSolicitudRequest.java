package cl.duoc.appbelmonte.estadoSolicitud.dto;

import jakarta.validation.constraints.NotBlank;

public record EstadoSolicitudRequest(
		@NotBlank(message = "El nombre del estado es obligatorio") String nombre) {
}
