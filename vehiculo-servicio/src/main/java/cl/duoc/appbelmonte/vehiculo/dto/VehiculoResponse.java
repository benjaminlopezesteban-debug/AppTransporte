package cl.duoc.appbelmonte.vehiculo.dto;

public record VehiculoResponse(Long id, String patente, Integer anio, Long modeloId,
		String modeloNombre, String marcaNombre) {
}
