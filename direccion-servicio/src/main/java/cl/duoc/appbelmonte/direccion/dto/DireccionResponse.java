package cl.duoc.appbelmonte.direccion.dto;

public record DireccionResponse(Long id, String calle, Double latitud, Double longitud,
		Long comunaId, String comunaNombre, Long regionId, String regionNombre) {
}
