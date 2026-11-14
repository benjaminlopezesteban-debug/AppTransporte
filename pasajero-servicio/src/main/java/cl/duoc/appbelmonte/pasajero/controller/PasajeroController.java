package cl.duoc.appbelmonte.pasajero.controller;

import cl.duoc.appbelmonte.pasajero.dto.PasajeroRequest;
import cl.duoc.appbelmonte.pasajero.dto.PasajeroResponse;
import cl.duoc.appbelmonte.pasajero.service.PasajeroService;
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
@RequestMapping("/api/pasajeros")
public class PasajeroController {

	private final PasajeroService pasajeroService;

	public PasajeroController(PasajeroService pasajeroService) {
		this.pasajeroService = pasajeroService;
	}

	@PostMapping
	@PreAuthorize("hasAnyRole('SOLICITANTE', 'ADMINISTRADOR')")
	public ResponseEntity<PasajeroResponse> crear(
			@Valid @RequestBody PasajeroRequest solicitud) {
		return ResponseEntity.status(HttpStatus.CREATED).body(pasajeroService.crear(solicitud));
	}

	@GetMapping("/{id}")
	public PasajeroResponse obtener(@PathVariable Long id) {
		return pasajeroService.obtener(id);
	}
}
