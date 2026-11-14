package cl.duoc.appbelmonte.historial.dto;

import java.time.LocalDateTime;

public record HistorialResponse(Long id, LocalDateTime fechaHora, String detalle, Long viajeId,
		Long estadoAnteriorId, String estadoAnteriorNombre, Long estadoNuevoId,
		String estadoNuevoNombre, Long cambiadoPorId) {
}
