package cl.duoc.appbelmonte.historial.controller;

import cl.duoc.appbelmonte.historial.dto.HistorialRequest;
import cl.duoc.appbelmonte.historial.dto.HistorialResponse;
import cl.duoc.appbelmonte.historial.service.HistorialService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/historial")
public class HistorialController {

	private final HistorialService historialService;

	public HistorialController(HistorialService historialService) {
		this.historialService = historialService;
	}

	@PostMapping
	@PreAuthorize("hasAnyRole('ADMINISTRADOR', 'CONDUCTOR')")
	public ResponseEntity<HistorialResponse> registrar(
			@AuthenticationPrincipal Long cambiadoPorId,
			@Valid @RequestBody HistorialRequest solicitud) {
		return ResponseEntity.status(HttpStatus.CREATED)
				.body(historialService.registrar(solicitud, cambiadoPorId));
	}

	@GetMapping("/viaje/{viajeId}")
	@PreAuthorize("hasRole('ADMINISTRADOR')")
	public List<HistorialResponse> listarPorViaje(@PathVariable Long viajeId) {
		return historialService.listarPorViaje(viajeId);
	}
}
