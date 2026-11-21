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
	public ResponseEntity<ViajeResponse> asignar(
			@Valid @RequestBody AsignacionRequest solicitud) {
		return ResponseEntity.status(HttpStatus.CREATED)
				.body(viajeService.asignar(solicitud, solicitud.asignadoPorId()));
	}

	@PutMapping("/{id}/reasignar")
	public ViajeResponse reasignar(@PathVariable Long id,
			@Valid @RequestBody ReasignacionRequest solicitud) {
		return viajeService.reasignar(id, solicitud, solicitud.asignadoPorId());
	}

	@GetMapping
	public List<ViajeResponse> listar(@RequestParam(required = false)
			@DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fecha) {
		return viajeService.listar(fecha);
	}

	@GetMapping("/mis-viajes/{conductorId}")
	public List<ViajeResponse> listarMios(@PathVariable Long conductorId) {
		return viajeService.listarMios(conductorId);
	}

	@GetMapping("/mis-viajes/{conductorId}/hoy")
	public List<ViajeResponse> listarHoy(@PathVariable Long conductorId) {
		return viajeService.listarHoy(conductorId);
	}

	@GetMapping("/{id}")
	public ViajeResponse obtener(@PathVariable Long id) {
		return viajeService.obtener(id);
	}

	@PutMapping("/{id}/iniciar")
	public ViajeResponse iniciar(@PathVariable Long id) {
		return viajeService.iniciar(id);
	}

	@PutMapping("/{id}/finalizar")
	public ViajeResponse finalizar(@PathVariable Long id) {
		return viajeService.finalizar(id);
	}
}
