package cl.duoc.appbelmonte.plantilla.config;

import cl.duoc.appbelmonte.plantilla.model.Rol;
import cl.duoc.appbelmonte.plantilla.model.Usuario;
import cl.duoc.appbelmonte.plantilla.repository.UsuarioRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
public class DatosPruebaConfig {

	private static final String CONTRASENA_PRUEBA = "clave123";

	@Bean
	public CommandLineRunner cargarDatosDePrueba(UsuarioRepository usuarioRepository,
			PasswordEncoder passwordEncoder) {
		return args -> {
			if (usuarioRepository.count() > 0) {
				return;
			}

			usuarioRepository.save(crearUsuario("Admin Belmonte", "admin@appbelmonte.cl",
					"+56911111111", Rol.ADMINISTRADOR, passwordEncoder));
			usuarioRepository.save(crearUsuario("Carla Soto", "carla.soto@ficticio.cl",
					"+56922222222", Rol.SOLICITANTE, passwordEncoder));
			usuarioRepository.save(crearUsuario("Diego Rojas", "diego.rojas@ficticio.cl",
					"+56933333333", Rol.SOLICITANTE, passwordEncoder));
			usuarioRepository.save(crearUsuario("Pedro Vera", "pedro.vera@ficticio.cl",
					"+56944444444", Rol.CONDUCTOR, passwordEncoder));
			usuarioRepository.save(crearUsuario("María Lagos", "maria.lagos@ficticio.cl",
					"+56955555555", Rol.CONDUCTOR, passwordEncoder));
			usuarioRepository.save(crearUsuario("Luis Fuentes", "luis.fuentes@ficticio.cl",
					"+56966666666", Rol.CONDUCTOR, passwordEncoder));
		};
	}

	private Usuario crearUsuario(String nombre, String correo, String telefono, Rol rol,
			PasswordEncoder passwordEncoder) {
		Usuario usuario = new Usuario();
		usuario.setNombre(nombre);
		usuario.setCorreo(correo);
		usuario.setContrasena(passwordEncoder.encode(CONTRASENA_PRUEBA));
		usuario.setTelefono(telefono);
		usuario.setRol(rol);
		usuario.setActivo(true);
		return usuario;
	}
}
