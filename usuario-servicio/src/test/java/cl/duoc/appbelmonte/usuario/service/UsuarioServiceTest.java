package cl.duoc.appbelmonte.usuario.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import cl.duoc.appbelmonte.usuario.client.RolClient;
import cl.duoc.appbelmonte.usuario.client.RolResponse;
import cl.duoc.appbelmonte.usuario.dto.LoginRequest;
import cl.duoc.appbelmonte.usuario.dto.LoginResponse;
import cl.duoc.appbelmonte.usuario.exception.CredencialesInvalidasException;
import cl.duoc.appbelmonte.usuario.model.Usuario;
import cl.duoc.appbelmonte.usuario.repository.UsuarioRepository;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class UsuarioServiceTest {

	@Mock
	private UsuarioRepository usuarioRepository;

	@Mock
	private RolClient rolClient;

	@InjectMocks
	private UsuarioService usuarioService;

	@Test
	void loginValidoDevuelveRolYNombre() {
		Usuario usuario = usuario(1L, "Admin Belmonte", "admin@appbelmonte.cl", "clave123",
				true, 1L);
		when(usuarioRepository.findByCorreo("admin@appbelmonte.cl"))
				.thenReturn(Optional.of(usuario));
		when(rolClient.obtener(1L)).thenReturn(new RolResponse(1L, "ADMINISTRADOR"));

		LoginResponse respuesta = usuarioService
				.login(new LoginRequest("admin@appbelmonte.cl", "clave123"));

		assertThat(respuesta.id()).isEqualTo(1L);
		assertThat(respuesta.rolId()).isEqualTo(1L);
		assertThat(respuesta.rolNombre()).isEqualTo("ADMINISTRADOR");
		assertThat(respuesta.nombre()).isEqualTo("Admin Belmonte");
	}

	@Test
	void loginConContrasenaInvalidaLanzaCredencialesInvalidas() {
		Usuario usuario = usuario(1L, "Admin Belmonte", "admin@appbelmonte.cl", "clave123",
				true, 1L);
		when(usuarioRepository.findByCorreo("admin@appbelmonte.cl"))
				.thenReturn(Optional.of(usuario));

		assertThatThrownBy(() -> usuarioService
				.login(new LoginRequest("admin@appbelmonte.cl", "incorrecta")))
				.isInstanceOf(CredencialesInvalidasException.class);
		verifyNoInteractions(rolClient);
	}

	@Test
	void loginConCorreoInexistenteLanzaCredencialesInvalidas() {
		when(usuarioRepository.findByCorreo("nadie@appbelmonte.cl"))
				.thenReturn(Optional.empty());

		assertThatThrownBy(() -> usuarioService
				.login(new LoginRequest("nadie@appbelmonte.cl", "clave123")))
				.isInstanceOf(CredencialesInvalidasException.class);
		verifyNoInteractions(rolClient);
	}

	@Test
	void loginConUsuarioInactivoLanzaCredencialesInvalidas() {
		Usuario usuario = usuario(1L, "Admin Belmonte", "admin@appbelmonte.cl", "clave123",
				false, 1L);
		when(usuarioRepository.findByCorreo("admin@appbelmonte.cl"))
				.thenReturn(Optional.of(usuario));

		assertThatThrownBy(() -> usuarioService
				.login(new LoginRequest("admin@appbelmonte.cl", "clave123")))
				.isInstanceOf(CredencialesInvalidasException.class);
		verifyNoInteractions(rolClient);
	}

	private Usuario usuario(Long id, String nombre, String correo, String contrasena,
			Boolean activo, Long rolId) {
		Usuario usuario = new Usuario();
		usuario.setId(id);
		usuario.setNombre(nombre);
		usuario.setCorreo(correo);
		usuario.setContrasena(contrasena);
		usuario.setActivo(activo);
		usuario.setRolId(rolId);
		return usuario;
	}
}
