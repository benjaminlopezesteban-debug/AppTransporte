package cl.duoc.appbelmonte.conductor.controller;

import cl.duoc.appbelmonte.conductor.dto.ConductorRequest;
import cl.duoc.appbelmonte.conductor.dto.ConductorResponse;
import cl.duoc.appbelmonte.conductor.dto.DisponibilidadRequest;
import cl.duoc.appbelmonte.conductor.service.ConductorService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/conductores")
public class ConductorController {

	private final ConductorService conductorService;

	public ConductorController(ConductorService conductorService) {
		this.conductorService = conductorService;
	}

	@PostMapping
	@PreAuthorize("hasRole('CONDUCTOR')")
	public ResponseEntity<ConductorResponse> crear(@AuthenticationPrincipal Long conductorId,
			@Valid @RequestBody ConductorRequest solicitud) {
		return ResponseEntity.status(HttpStatus.CREATED)
				.body(conductorService.crear(conductorId, solicitud));
	}

	@GetMapping("/yo")
	@PreAuthorize("hasRole('CONDUCTOR')")
	public ConductorResponse obtenerYo(@AuthenticationPrincipal Long conductorId) {
		return conductorService.obtenerYo(conductorId);
	}

	@PutMapping("/yo/disponibilidad")
	@PreAuthorize("hasRole('CONDUCTOR')")
	public ConductorResponse cambiarDisponibilidad(@AuthenticationPrincipal Long conductorId,
			@Valid @RequestBody DisponibilidadRequest solicitud) {
		return conductorService.cambiarDisponibilidad(conductorId, solicitud.disponible());
	}

	@GetMapping("/disponibles")
	@PreAuthorize("hasRole('ADMINISTRADOR')")
	public List<ConductorResponse> listarDisponibles() {
		return conductorService.listarDisponibles();
	}

	@GetMapping("/{id}")
	@PreAuthorize("hasRole('ADMINISTRADOR')")
	public ConductorResponse obtener(@PathVariable Long id) {
		return conductorService.obtener(id);
	}
}
