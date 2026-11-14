package cl.duoc.appbelmonte.direccion.controller;

import cl.duoc.appbelmonte.direccion.dto.DireccionRequest;
import cl.duoc.appbelmonte.direccion.dto.DireccionResponse;
import cl.duoc.appbelmonte.direccion.service.DireccionService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/direcciones")
public class DireccionController {

	private final DireccionService direccionService;

	public DireccionController(DireccionService direccionService) {
		this.direccionService = direccionService;
	}

	@PostMapping
	@PreAuthorize("hasAnyRole('SOLICITANTE', 'ADMINISTRADOR')")
	public ResponseEntity<DireccionResponse> crear(
			@Valid @RequestBody DireccionRequest solicitud) {
		return ResponseEntity.status(HttpStatus.CREATED).body(direccionService.crear(solicitud));
	}

	@GetMapping("/{id}")
	public DireccionResponse obtener(@PathVariable Long id) {
		return direccionService.obtener(id);
	}
}
