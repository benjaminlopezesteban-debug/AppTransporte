package cl.duoc.appbelmonte.modeloVehiculo.controller;

import cl.duoc.appbelmonte.modeloVehiculo.dto.ModeloVehiculoRequest;
import cl.duoc.appbelmonte.modeloVehiculo.dto.ModeloVehiculoResponse;
import cl.duoc.appbelmonte.modeloVehiculo.service.ModeloVehiculoService;
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
@RequestMapping("/api/modelos")
public class ModeloVehiculoController {

	private final ModeloVehiculoService modeloVehiculoService;

	public ModeloVehiculoController(ModeloVehiculoService modeloVehiculoService) {
		this.modeloVehiculoService = modeloVehiculoService;
	}

	@GetMapping
	public List<ModeloVehiculoResponse> listar(@RequestParam(required = false) Long marcaId) {
		return modeloVehiculoService.listar(marcaId);
	}

	@GetMapping("/{id}")
	public ModeloVehiculoResponse obtener(@PathVariable Long id) {
		return modeloVehiculoService.obtener(id);
	}

	@PostMapping
	@PreAuthorize("hasRole('ADMINISTRADOR')")
	public ResponseEntity<ModeloVehiculoResponse> crear(
			@Valid @RequestBody ModeloVehiculoRequest solicitud) {
		return ResponseEntity.status(HttpStatus.CREATED)
				.body(modeloVehiculoService.crear(solicitud));
	}

	@PutMapping("/{id}")
	@PreAuthorize("hasRole('ADMINISTRADOR')")
	public ModeloVehiculoResponse actualizar(@PathVariable Long id,
			@Valid @RequestBody ModeloVehiculoRequest solicitud) {
		return modeloVehiculoService.actualizar(id, solicitud);
	}
}
