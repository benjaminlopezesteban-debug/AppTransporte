package cl.duoc.appbelmonte.vehiculo.controller;

import cl.duoc.appbelmonte.vehiculo.dto.VehiculoRequest;
import cl.duoc.appbelmonte.vehiculo.dto.VehiculoResponse;
import cl.duoc.appbelmonte.vehiculo.service.VehiculoService;
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
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/vehiculos")
public class VehiculoController {

	private final VehiculoService vehiculoService;

	public VehiculoController(VehiculoService vehiculoService) {
		this.vehiculoService = vehiculoService;
	}

	@GetMapping
	@PreAuthorize("hasRole('ADMINISTRADOR')")
	public List<VehiculoResponse> listar() {
		return vehiculoService.listar();
	}

	@GetMapping("/{id}")
	@PreAuthorize("hasAnyRole('ADMINISTRADOR', 'CONDUCTOR')")
	public VehiculoResponse obtener(@PathVariable Long id) {
		return vehiculoService.obtener(id);
	}

	@PostMapping
	@PreAuthorize("hasRole('ADMINISTRADOR')")
	public ResponseEntity<VehiculoResponse> crear(@Valid @RequestBody VehiculoRequest solicitud) {
		return ResponseEntity.status(HttpStatus.CREATED).body(vehiculoService.crear(solicitud));
	}

	@PutMapping("/{id}")
	@PreAuthorize("hasRole('ADMINISTRADOR')")
	public VehiculoResponse actualizar(@PathVariable Long id,
			@Valid @RequestBody VehiculoRequest solicitud) {
		return vehiculoService.actualizar(id, solicitud);
	}
}
