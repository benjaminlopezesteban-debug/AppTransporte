package cl.duoc.appbelmonte.solicitud.dto;

public record DireccionResponse(Long id, String calle, Double latitud, Double longitud,
		Long comunaId, String comunaNombre, Long regionId, String regionNombre) {
}
