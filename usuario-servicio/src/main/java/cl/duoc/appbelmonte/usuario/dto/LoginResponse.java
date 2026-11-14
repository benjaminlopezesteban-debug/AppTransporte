package cl.duoc.appbelmonte.usuario.dto;

public record LoginResponse(String token, Long id, Long rolId, String rolNombre, String nombre) {
}
