package cl.duoc.appbelmonte.usuario.dto;

public record UsuarioResponse(Long id, String nombre, String correo, String telefono,
		Boolean activo, Long rolId, String rolNombre) {
}
