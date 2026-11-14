package cl.duoc.appbelmonte.rol.controller;

import cl.duoc.appbelmonte.rol.dto.RolRequest;
import cl.duoc.appbelmonte.rol.dto.RolResponse;
import cl.duoc.appbelmonte.rol.service.RolService;
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
@RequestMapping("/api/roles")
public class RolController {

	private final RolService rolService;

	public RolController(RolService rolService) {
		this.rolService = rolService;
	}

	@GetMapping
	public List<RolResponse> listar() {
		return rolService.listar();
	}

	@GetMapping("/{id}")
	public RolResponse obtener(@PathVariable Long id) {
		return rolService.obtener(id);
	}

	@GetMapping("/nombre/{nombre}")
	public RolResponse obtenerPorNombre(@PathVariable String nombre) {
		return rolService.obtenerPorNombre(nombre);
	}

	@PostMapping
	@PreAuthorize("hasRole('ADMINISTRADOR')")
	public ResponseEntity<RolResponse> crear(@Valid @RequestBody RolRequest solicitud) {
		return ResponseEntity.status(HttpStatus.CREATED).body(rolService.crear(solicitud));
	}

	@PutMapping("/{id}")
	@PreAuthorize("hasRole('ADMINISTRADOR')")
	public RolResponse actualizar(@PathVariable Long id, @Valid @RequestBody RolRequest solicitud) {
		return rolService.actualizar(id, solicitud);
	}
}
