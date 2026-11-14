package cl.duoc.appbelmonte.usuario.service;

import cl.duoc.appbelmonte.usuario.client.RolClient;
import cl.duoc.appbelmonte.usuario.client.RolResponse;
import cl.duoc.appbelmonte.usuario.config.JwtEmisor;
import cl.duoc.appbelmonte.usuario.dto.LoginRequest;
import cl.duoc.appbelmonte.usuario.dto.LoginResponse;
import cl.duoc.appbelmonte.usuario.dto.RegistroRequest;
import cl.duoc.appbelmonte.usuario.dto.UsuarioResponse;
import cl.duoc.appbelmonte.usuario.exception.CredencialesInvalidasException;
import cl.duoc.appbelmonte.usuario.exception.RecursoNoEncontradoException;
import cl.duoc.appbelmonte.usuario.exception.ReglaNegocioException;
import cl.duoc.appbelmonte.usuario.model.Usuario;
import cl.duoc.appbelmonte.usuario.repository.UsuarioRepository;
import java.util.List;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UsuarioService {

	private static final List<Long> ROLES_REGISTRO_PUBLICO = List.of(2L, 3L);

	private final UsuarioRepository usuarioRepository;
	private final PasswordEncoder passwordEncoder;
	private final JwtEmisor jwtEmisor;
	private final RolClient rolClient;

	public UsuarioService(UsuarioRepository usuarioRepository, PasswordEncoder passwordEncoder,
			JwtEmisor jwtEmisor, RolClient rolClient) {
		this.usuarioRepository = usuarioRepository;
		this.passwordEncoder = passwordEncoder;
		this.jwtEmisor = jwtEmisor;
		this.rolClient = rolClient;
	}

	@Transactional
	public void registrar(RegistroRequest solicitud) {
		if (!ROLES_REGISTRO_PUBLICO.contains(solicitud.rolId())) {
			throw new ReglaNegocioException(
					"El registro público solo permite los roles SOLICITANTE y CONDUCTOR");
		}
		if (usuarioRepository.existsByCorreo(solicitud.correo())) {
			throw new ReglaNegocioException("El correo ya está registrado");
		}

		RolResponse rol = rolClient.obtener(solicitud.rolId());

		Usuario usuario = new Usuario();
		usuario.setNombre(solicitud.nombre());
		usuario.setCorreo(solicitud.correo());
		usuario.setContrasena(passwordEncoder.encode(solicitud.contrasena()));
		usuario.setTelefono(solicitud.telefono());
		usuario.setRolId(rol.id());
		usuario.setActivo(true);
		usuarioRepository.save(usuario);
	}

	@Transactional(readOnly = true)
	public LoginResponse login(LoginRequest solicitud) {
		Usuario usuario = usuarioRepository.findByCorreo(solicitud.correo())
				.orElseThrow(() -> new CredencialesInvalidasException("Credenciales inválidas"));

		if (!Boolean.TRUE.equals(usuario.getActivo())
				|| !passwordEncoder.matches(solicitud.contrasena(), usuario.getContrasena())) {
			throw new CredencialesInvalidasException("Credenciales inválidas");
		}

		RolResponse rol = rolClient.obtener(usuario.getRolId());
		String token = jwtEmisor.generarToken(usuario, rol.nombre());
		return new LoginResponse(token, usuario.getId(), usuario.getRolId(), rol.nombre(),
				usuario.getNombre());
	}

	@Transactional(readOnly = true)
	public UsuarioResponse obtenerYo(Long usuarioId) {
		Usuario usuario = usuarioRepository.findById(usuarioId)
				.orElseThrow(() -> new RecursoNoEncontradoException("El usuario no existe"));
		RolResponse rol = rolClient.obtener(usuario.getRolId());
		return new UsuarioResponse(usuario.getId(), usuario.getNombre(), usuario.getCorreo(),
				usuario.getTelefono(), usuario.getActivo(), usuario.getRolId(), rol.nombre());
	}
}
