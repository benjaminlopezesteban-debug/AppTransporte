package cl.duoc.appbelmonte.region.controller;

import cl.duoc.appbelmonte.region.dto.RegionRequest;
import cl.duoc.appbelmonte.region.dto.RegionResponse;
import cl.duoc.appbelmonte.region.service.RegionService;
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
@RequestMapping("/api/regiones")
public class RegionController {

	private final RegionService regionService;

	public RegionController(RegionService regionService) {
		this.regionService = regionService;
	}

	@GetMapping
	public List<RegionResponse> listar() {
		return regionService.listar();
	}

	@GetMapping("/{id}")
	public RegionResponse obtener(@PathVariable Long id) {
		return regionService.obtener(id);
	}

	@PostMapping
	public ResponseEntity<RegionResponse> crear(@Valid @RequestBody RegionRequest solicitud) {
		return ResponseEntity.status(HttpStatus.CREATED).body(regionService.crear(solicitud));
	}

	@PutMapping("/{id}")
	public RegionResponse actualizar(@PathVariable Long id, @Valid @RequestBody RegionRequest solicitud) {
		return regionService.actualizar(id, solicitud);
	}
}
