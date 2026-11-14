package cl.duoc.appbelmonte.estadoSolicitud.controller;

import cl.duoc.appbelmonte.estadoSolicitud.dto.EstadoSolicitudRequest;
import cl.duoc.appbelmonte.estadoSolicitud.dto.EstadoSolicitudResponse;
import cl.duoc.appbelmonte.estadoSolicitud.service.EstadoSolicitudService;
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
@RequestMapping("/api/estados-solicitud")
public class EstadoSolicitudController {

	private final EstadoSolicitudService estadoSolicitudService;

	public EstadoSolicitudController(EstadoSolicitudService estadoSolicitudService) {
		this.estadoSolicitudService = estadoSolicitudService;
	}

	@GetMapping
	public List<EstadoSolicitudResponse> listar() {
		return estadoSolicitudService.listar();
	}

	@GetMapping("/{id}")
	public EstadoSolicitudResponse obtener(@PathVariable Long id) {
		return estadoSolicitudService.obtener(id);
	}

	@GetMapping("/nombre/{nombre}")
	public EstadoSolicitudResponse obtenerPorNombre(@PathVariable String nombre) {
		return estadoSolicitudService.obtenerPorNombre(nombre);
	}

	@PostMapping
	@PreAuthorize("hasRole('ADMINISTRADOR')")
	public ResponseEntity<EstadoSolicitudResponse> crear(
			@Valid @RequestBody EstadoSolicitudRequest solicitud) {
		return ResponseEntity.status(HttpStatus.CREATED)
				.body(estadoSolicitudService.crear(solicitud));
	}

	@PutMapping("/{id}")
	@PreAuthorize("hasRole('ADMINISTRADOR')")
	public EstadoSolicitudResponse actualizar(@PathVariable Long id,
			@Valid @RequestBody EstadoSolicitudRequest solicitud) {
		return estadoSolicitudService.actualizar(id, solicitud);
	}
}
