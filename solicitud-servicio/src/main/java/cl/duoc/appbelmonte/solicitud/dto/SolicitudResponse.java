package cl.duoc.appbelmonte.solicitud.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

public record SolicitudResponse(Long id, LocalDate fecha, LocalTime hora, String numeroVuelo,
		String observaciones, LocalDateTime fechaCreacion, Long estadoId, String estadoNombre,
		Long solicitanteId, PasajeroResponse pasajero, DireccionResponse origen,
		DireccionResponse destino) {
}
