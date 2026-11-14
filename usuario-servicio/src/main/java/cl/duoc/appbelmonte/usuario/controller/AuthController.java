package cl.duoc.appbelmonte.usuario.controller;

import cl.duoc.appbelmonte.usuario.dto.LoginRequest;
import cl.duoc.appbelmonte.usuario.dto.LoginResponse;
import cl.duoc.appbelmonte.usuario.dto.RegistroRequest;
import cl.duoc.appbelmonte.usuario.service.UsuarioService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

	private final UsuarioService usuarioService;

	public AuthController(UsuarioService usuarioService) {
		this.usuarioService = usuarioService;
	}

	@PostMapping("/registro")
	public ResponseEntity<Void> registrar(@Valid @RequestBody RegistroRequest solicitud) {
		usuarioService.registrar(solicitud);
		return ResponseEntity.status(HttpStatus.CREATED).build();
	}

	@PostMapping("/login")
	public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest solicitud) {
		return ResponseEntity.ok(usuarioService.login(solicitud));
	}
}
