package cl.duoc.appbelmonte.viaje.controller;

import cl.duoc.appbelmonte.viaje.dto.AsignacionRequest;
import cl.duoc.appbelmonte.viaje.dto.ReasignacionRequest;
import cl.duoc.appbelmonte.viaje.dto.ViajeResponse;
import cl.duoc.appbelmonte.viaje.service.ViajeService;
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
@RequestMapping("/api/viajes")
public class ViajeController {

	private final ViajeService viajeService;

	public ViajeController(ViajeService viajeService) {
		this.viajeService = viajeService;
	}

	@PostMapping
	@PreAuthorize("hasRole('ADMINISTRADOR')")
	public ResponseEntity<ViajeResponse> asignar(
			@AuthenticationPrincipal Long asignadoPorId,
			@Valid @RequestBody AsignacionRequest solicitud) {
		return ResponseEntity.status(HttpStatus.CREATED)
				.body(viajeService.asignar(solicitud, asignadoPorId));
	}

	@PutMapping("/{id}/reasignar")
	@PreAuthorize("hasRole('ADMINISTRADOR')")
	public ViajeResponse reasignar(@PathVariable Long id,
			@AuthenticationPrincipal Long asignadoPorId,
			@Valid @RequestBody ReasignacionRequest solicitud) {
		return viajeService.reasignar(id, solicitud, asignadoPorId);
	}

	@GetMapping
	@PreAuthorize("hasRole('ADMINISTRADOR')")
	public List<ViajeResponse> listar(@RequestParam(required = false)
			@DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fecha) {
		return viajeService.listar(fecha);
	}

	@GetMapping("/mis-viajes")
	@PreAuthorize("hasRole('CONDUCTOR')")
	public List<ViajeResponse> listarMios(@AuthenticationPrincipal Long conductorId) {
		return viajeService.listarMios(conductorId);
	}

	@GetMapping("/mis-viajes/hoy")
	@PreAuthorize("hasRole('CONDUCTOR')")
	public List<ViajeResponse> listarHoy(@AuthenticationPrincipal Long conductorId) {
		return viajeService.listarHoy(conductorId);
	}

	@GetMapping("/{id}")
	@PreAuthorize("hasAnyRole('ADMINISTRADOR', 'CONDUCTOR', 'SOLICITANTE')")
	public ViajeResponse obtener(@PathVariable Long id,
			@AuthenticationPrincipal Long usuarioId, Authentication authentication) {
		return viajeService.obtener(id, usuarioId, rol(authentication));
	}

	@PutMapping("/{id}/iniciar")
	@PreAuthorize("hasRole('CONDUCTOR')")
	public ViajeResponse iniciar(@PathVariable Long id,
			@AuthenticationPrincipal Long conductorId) {
		return viajeService.iniciar(id, conductorId);
	}

	@PutMapping("/{id}/finalizar")
	@PreAuthorize("hasRole('CONDUCTOR')")
	public ViajeResponse finalizar(@PathVariable Long id,
			@AuthenticationPrincipal Long conductorId) {
		return viajeService.finalizar(id, conductorId);
	}

	private String rol(Authentication authentication) {
		return authentication.getAuthorities().stream()
				.map(GrantedAuthority::getAuthority)
				.filter(autoridad -> autoridad.startsWith("ROLE_"))
				.map(autoridad -> autoridad.substring("ROLE_".length()))
				.findFirst()
				.orElse("");
	}
}
