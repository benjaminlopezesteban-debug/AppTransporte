package cl.duoc.appbelmonte.conductor.controller;

import cl.duoc.appbelmonte.conductor.dto.ConductorRequest;
import cl.duoc.appbelmonte.conductor.dto.ConductorResponse;
import cl.duoc.appbelmonte.conductor.dto.DisponibilidadRequest;
import cl.duoc.appbelmonte.conductor.service.ConductorService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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

	@PostMapping("/{usuarioId}")
	public ResponseEntity<ConductorResponse> crear(@PathVariable Long usuarioId,
			@Valid @RequestBody ConductorRequest solicitud) {
		return ResponseEntity.status(HttpStatus.CREATED)
				.body(conductorService.crear(usuarioId, solicitud));
	}

	@GetMapping("/disponibles")
	public List<ConductorResponse> listarDisponibles() {
		return conductorService.listarDisponibles();
	}

	@GetMapping("/{id}")
	public ConductorResponse obtener(@PathVariable Long id) {
		return conductorService.obtener(id);
	}

	@PutMapping("/{id}/disponibilidad")
	public ConductorResponse cambiarDisponibilidad(@PathVariable Long id,
			@Valid @RequestBody DisponibilidadRequest solicitud) {
		return conductorService.cambiarDisponibilidad(id, solicitud.disponible());
	}
}
