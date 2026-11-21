package cl.duoc.appbelmonte.solicitud.controller;

import cl.duoc.appbelmonte.solicitud.dto.SolicitudRequest;
import cl.duoc.appbelmonte.solicitud.dto.SolicitudResponse;
import cl.duoc.appbelmonte.solicitud.service.SolicitudService;
import jakarta.validation.Valid;
import java.time.LocalDate;
import java.util.List;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/solicitudes")
public class SolicitudController {

	private final SolicitudService solicitudService;

	public SolicitudController(SolicitudService solicitudService) {
		this.solicitudService = solicitudService;
	}

	@PostMapping
	public ResponseEntity<SolicitudResponse> crear(
			@Valid @RequestBody SolicitudRequest solicitud) {
		return ResponseEntity.status(HttpStatus.CREATED)
				.body(solicitudService.crear(solicitud, solicitud.solicitanteId()));
	}

	@GetMapping("/mias/{solicitanteId}")
	public List<SolicitudResponse> listarMias(@PathVariable Long solicitanteId) {
		return solicitudService.listarMias(solicitanteId);
	}

	@PutMapping("/{id}/cancelar")
	public SolicitudResponse cancelar(@PathVariable Long id) {
		return solicitudService.cancelar(id);
	}

	@GetMapping(params = "ids")
	public List<SolicitudResponse> listarPorIds(@RequestParam List<Long> ids,
			@RequestParam(required = false)
			@DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fecha) {
		return solicitudService.listarPorIds(ids, fecha);
	}

	@GetMapping
	public List<SolicitudResponse> listar(@RequestParam(required = false) Long estado,
			@RequestParam(required = false)
			@DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fecha) {
		return solicitudService.listar(estado, fecha);
	}

	@GetMapping("/{id}")
	public SolicitudResponse obtener(@PathVariable Long id) {
		return solicitudService.obtener(id);
	}

	@PutMapping("/{id}/asignar")
	public SolicitudResponse asignar(@PathVariable Long id) {
		return solicitudService.asignar(id);
	}
}
