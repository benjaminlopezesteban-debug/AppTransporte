package cl.duoc.appbelmonte.conductor.dto;

public record ConductorResponse(Long id, String numeroLicencia, Boolean disponible,
		Long vehiculoId, String vehiculoPatente, String modeloNombre, String marcaNombre) {
}
