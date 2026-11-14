package cl.duoc.appbelmonte.comuna.controller;

import cl.duoc.appbelmonte.comuna.dto.ComunaRequest;
import cl.duoc.appbelmonte.comuna.dto.ComunaResponse;
import cl.duoc.appbelmonte.comuna.service.ComunaService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/comunas")
public class ComunaController {

	private final ComunaService comunaService;

	public ComunaController(ComunaService comunaService) {
		this.comunaService = comunaService;
	}

	@GetMapping
	public List<ComunaResponse> listar(@RequestParam(required = false) Long regionId) {
		return comunaService.listar(regionId);
	}

	@GetMapping("/{id}")
	public ComunaResponse obtener(@PathVariable Long id) {
		return comunaService.obtener(id);
	}

	@PostMapping
	@PreAuthorize("hasRole('ADMINISTRADOR')")
	public ResponseEntity<ComunaResponse> crear(@Valid @RequestBody ComunaRequest solicitud) {
		return ResponseEntity.status(HttpStatus.CREATED).body(comunaService.crear(solicitud));
	}

	@PutMapping("/{id}")
	@PreAuthorize("hasRole('ADMINISTRADOR')")
	public ComunaResponse actualizar(@PathVariable Long id, @Valid @RequestBody ComunaRequest solicitud) {
		return comunaService.actualizar(id, solicitud);
	}
}
