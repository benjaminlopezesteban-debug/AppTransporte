package cl.duoc.appbelmonte.viaje.dto;

import java.time.LocalDateTime;

public record ViajeResponse(Long id, LocalDateTime fechaAsignacion, LocalDateTime horaInicioReal,
		LocalDateTime horaFinReal, Long solicitudId, Long conductorId, Long asignadoPorId,
		Long estadoId, String estadoNombre, SolicitudResponse solicitud) {
}
