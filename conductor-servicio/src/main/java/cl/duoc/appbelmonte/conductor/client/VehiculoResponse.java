package cl.duoc.appbelmonte.conductor.client;

public record VehiculoResponse(Long id, String patente, Integer anio, Long modeloId,
		String modeloNombre, String marcaNombre) {
}
