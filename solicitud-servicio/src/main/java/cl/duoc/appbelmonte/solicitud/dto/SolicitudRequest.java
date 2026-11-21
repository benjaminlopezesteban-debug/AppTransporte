package cl.duoc.appbelmonte.solicitud.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import java.time.LocalTime;

public record SolicitudRequest(
		@NotNull(message = "La fecha es obligatoria") LocalDate fecha,
		@NotNull(message = "La hora es obligatoria") LocalTime hora,
		String numeroVuelo,
		String observaciones,
		@NotNull(message = "Los datos del pasajero son obligatorios")
		@Valid PasajeroDatos pasajero,
		@NotNull(message = "La dirección de origen es obligatoria")
		@Valid DireccionDatos origen,
		@NotNull(message = "La dirección de destino es obligatoria")
		@Valid DireccionDatos destino,
		@NotNull(message = "El solicitante es obligatorio") Long solicitanteId) {
}
