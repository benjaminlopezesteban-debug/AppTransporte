package cl.duoc.appbelmonte.usuario.config;

import cl.duoc.appbelmonte.usuario.model.Usuario;
import cl.duoc.appbelmonte.usuario.repository.UsuarioRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
public class DatosPruebaConfig {

	private static final String CONTRASENA_PRUEBA = "clave123";

	@Bean
	public CommandLineRunner cargarUsuarios(UsuarioRepository usuarioRepository,
			PasswordEncoder passwordEncoder) {
		return args -> {
			if (usuarioRepository.count() > 0) {
				return;
			}

			usuarioRepository.save(crearUsuario("Admin Belmonte", "admin@appbelmonte.cl",
					"+56911111111", 1L, passwordEncoder));
			usuarioRepository.save(crearUsuario("Carla Soto", "carla.soto@ficticio.cl",
					"+56922222222", 2L, passwordEncoder));
			usuarioRepository.save(crearUsuario("Diego Rojas", "diego.rojas@ficticio.cl",
					"+56933333333", 2L, passwordEncoder));
			usuarioRepository.save(crearUsuario("Pedro Vera", "pedro.vera@ficticio.cl",
					"+56944444444", 3L, passwordEncoder));
			usuarioRepository.save(crearUsuario("María Lagos", "maria.lagos@ficticio.cl",
					"+56955555555", 3L, passwordEncoder));
			usuarioRepository.save(crearUsuario("Luis Fuentes", "luis.fuentes@ficticio.cl",
					"+56966666666", 3L, passwordEncoder));
		};
	}

	private Usuario crearUsuario(String nombre, String correo, String telefono, Long rolId,
			PasswordEncoder passwordEncoder) {
		Usuario usuario = new Usuario();
		usuario.setNombre(nombre);
		usuario.setCorreo(correo);
		usuario.setContrasena(passwordEncoder.encode(CONTRASENA_PRUEBA));
		usuario.setTelefono(telefono);
		usuario.setRolId(rolId);
		usuario.setActivo(true);
		return usuario;
	}
}
