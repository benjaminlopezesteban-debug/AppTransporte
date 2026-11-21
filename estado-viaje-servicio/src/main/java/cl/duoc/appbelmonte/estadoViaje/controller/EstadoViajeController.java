package cl.duoc.appbelmonte.estadoViaje.controller;

import cl.duoc.appbelmonte.estadoViaje.dto.EstadoViajeRequest;
import cl.duoc.appbelmonte.estadoViaje.dto.EstadoViajeResponse;
import cl.duoc.appbelmonte.estadoViaje.service.EstadoViajeService;
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
@RequestMapping("/api/estados-viaje")
public class EstadoViajeController {

	private final EstadoViajeService estadoViajeService;

	public EstadoViajeController(EstadoViajeService estadoViajeService) {
		this.estadoViajeService = estadoViajeService;
	}

	@GetMapping
	public List<EstadoViajeResponse> listar() {
		return estadoViajeService.listar();
	}

	@GetMapping("/{id}")
	public EstadoViajeResponse obtener(@PathVariable Long id) {
		return estadoViajeService.obtener(id);
	}

	@GetMapping("/nombre/{nombre}")
	public EstadoViajeResponse obtenerPorNombre(@PathVariable String nombre) {
		return estadoViajeService.obtenerPorNombre(nombre);
	}

	@PostMapping
	public ResponseEntity<EstadoViajeResponse> crear(
			@Valid @RequestBody EstadoViajeRequest solicitud) {
		return ResponseEntity.status(HttpStatus.CREATED).body(estadoViajeService.crear(solicitud));
	}

	@PutMapping("/{id}")
	public EstadoViajeResponse actualizar(@PathVariable Long id,
			@Valid @RequestBody EstadoViajeRequest solicitud) {
		return estadoViajeService.actualizar(id, solicitud);
	}
}
