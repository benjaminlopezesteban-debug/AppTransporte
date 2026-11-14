package cl.duoc.appbelmonte.plantilla.dto;

import cl.duoc.appbelmonte.plantilla.model.Rol;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record RegistroRequest(
		@NotBlank(message = "El nombre es obligatorio") String nombre,
		@NotBlank(message = "El correo es obligatorio")
		@Email(message = "El correo no tiene un formato válido") String correo,
		@NotBlank(message = "La contraseña es obligatoria") String contrasena,
		String telefono,
		@NotNull(message = "El rol es obligatorio") Rol rol) {
}
