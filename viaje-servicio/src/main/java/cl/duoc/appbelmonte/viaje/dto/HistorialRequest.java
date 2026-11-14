package cl.duoc.appbelmonte.viaje.dto;

import java.time.LocalDateTime;

public record HistorialRequest(LocalDateTime fechaHora, String detalle, Long viajeId,
		Long estadoAnteriorId, Long estadoNuevoId) {
}
