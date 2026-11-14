package cl.duoc.appbelmonte.plantilla.dto;

import cl.duoc.appbelmonte.plantilla.model.Rol;

public record LoginResponse(String token, Long id, Rol rol, String nombre) {
}
