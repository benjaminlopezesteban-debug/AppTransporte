package cl.duoc.appbelmonte.plantilla.service;

import cl.duoc.appbelmonte.plantilla.dto.LoginRequest;
import cl.duoc.appbelmonte.plantilla.dto.LoginResponse;
import cl.duoc.appbelmonte.plantilla.dto.RegistroRequest;
import cl.duoc.appbelmonte.plantilla.exception.CredencialesInvalidasException;
import cl.duoc.appbelmonte.plantilla.exception.ReglaNegocioException;
import cl.duoc.appbelmonte.plantilla.model.Rol;
import cl.duoc.appbelmonte.plantilla.model.Usuario;
import cl.duoc.appbelmonte.plantilla.repository.UsuarioRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UsuarioService {

	private final UsuarioRepository usuarioRepository;

	public UsuarioService(UsuarioRepository usuarioRepository) {
		this.usuarioRepository = usuarioRepository;
	}

	@Transactional
	public void registrar(RegistroRequest solicitud) {
		if (solicitud.rol() == Rol.ADMINISTRADOR) {
			throw new ReglaNegocioException(
					"El registro público solo permite los roles SOLICITANTE y CONDUCTOR");
		}
		if (usuarioRepository.existsByCorreo(solicitud.correo())) {
			throw new ReglaNegocioException("El correo ya está registrado");
		}

		Usuario usuario = new Usuario();
		usuario.setNombre(solicitud.nombre());
		usuario.setCorreo(solicitud.correo());
		usuario.setContrasena(solicitud.contrasena());
		usuario.setTelefono(solicitud.telefono());
		usuario.setRol(solicitud.rol());
		usuario.setActivo(true);
		usuarioRepository.save(usuario);
	}

	@Transactional(readOnly = true)
	public LoginResponse login(LoginRequest solicitud) {
		Usuario usuario = usuarioRepository.findByCorreo(solicitud.correo())
				.orElseThrow(() -> new CredencialesInvalidasException("Credenciales inválidas"));

		if (!Boolean.TRUE.equals(usuario.getActivo())
				|| !solicitud.contrasena().equals(usuario.getContrasena())) {
			throw new CredencialesInvalidasException("Credenciales inválidas");
		}

		return new LoginResponse(usuario.getId(), usuario.getRol(), usuario.getNombre());
	}
}
