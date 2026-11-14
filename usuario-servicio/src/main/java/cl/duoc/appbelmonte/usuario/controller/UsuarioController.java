package cl.duoc.appbelmonte.usuario.controller;

import cl.duoc.appbelmonte.usuario.dto.UsuarioResponse;
import cl.duoc.appbelmonte.usuario.service.UsuarioService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/usuarios")
public class UsuarioController {

	private final UsuarioService usuarioService;

	public UsuarioController(UsuarioService usuarioService) {
		this.usuarioService = usuarioService;
	}

	@GetMapping("/yo")
	public UsuarioResponse obtenerYo(@AuthenticationPrincipal Long usuarioId) {
		return usuarioService.obtenerYo(usuarioId);
	}
}
