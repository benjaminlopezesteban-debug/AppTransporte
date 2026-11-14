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
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
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
	@PreAuthorize("hasRole('SOLICITANTE')")
	public ResponseEntity<SolicitudResponse> crear(
			@AuthenticationPrincipal Long solicitanteId,
			@Valid @RequestBody SolicitudRequest solicitud) {
		return ResponseEntity.status(HttpStatus.CREATED)
				.body(solicitudService.crear(solicitud, solicitanteId));
	}

	@GetMapping("/mias")
	@PreAuthorize("hasRole('SOLICITANTE')")
	public List<SolicitudResponse> listarMias(@AuthenticationPrincipal Long solicitanteId) {
		return solicitudService.listarMias(solicitanteId);
	}

	@PutMapping("/{id}/cancelar")
	@PreAuthorize("hasRole('SOLICITANTE')")
	public SolicitudResponse cancelar(@PathVariable Long id,
			@AuthenticationPrincipal Long solicitanteId) {
		return solicitudService.cancelar(id, solicitanteId);
	}

	@GetMapping(params = "ids")
	@PreAuthorize("hasAnyRole('ADMINISTRADOR', 'CONDUCTOR')")
	public List<SolicitudResponse> listarPorIds(@RequestParam List<Long> ids,
			@RequestParam(required = false)
			@DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fecha) {
		return solicitudService.listarPorIds(ids, fecha);
	}

	@GetMapping
	@PreAuthorize("hasRole('ADMINISTRADOR')")
	public List<SolicitudResponse> listar(@RequestParam(required = false) Long estado,
			@RequestParam(required = false)
			@DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fecha) {
		return solicitudService.listar(estado, fecha);
	}

	@GetMapping("/{id}")
	@PreAuthorize("hasAnyRole('ADMINISTRADOR', 'CONDUCTOR', 'SOLICITANTE')")
	public SolicitudResponse obtener(@PathVariable Long id,
			@AuthenticationPrincipal Long usuarioId, Authentication authentication) {
		String rol = authentication.getAuthorities().stream()
				.map(GrantedAuthority::getAuthority)
				.filter(autoridad -> autoridad.startsWith("ROLE_"))
				.map(autoridad -> autoridad.substring("ROLE_".length()))
				.findFirst()
				.orElse("");
		return solicitudService.obtener(id, usuarioId, rol);
	}

	@PutMapping("/{id}/asignar")
	@PreAuthorize("hasRole('ADMINISTRADOR')")
	public SolicitudResponse asignar(@PathVariable Long id) {
		return solicitudService.asignar(id);
	}
}
