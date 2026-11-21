package cl.duoc.appbelmonte.marca.controller;

import cl.duoc.appbelmonte.marca.dto.MarcaRequest;
import cl.duoc.appbelmonte.marca.dto.MarcaResponse;
import cl.duoc.appbelmonte.marca.service.MarcaService;
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
@RequestMapping("/api/marcas")
public class MarcaController {

	private final MarcaService marcaService;

	public MarcaController(MarcaService marcaService) {
		this.marcaService = marcaService;
	}

	@GetMapping
	public List<MarcaResponse> listar() {
		return marcaService.listar();
	}

	@GetMapping("/{id}")
	public MarcaResponse obtener(@PathVariable Long id) {
		return marcaService.obtener(id);
	}

	@PostMapping
	public ResponseEntity<MarcaResponse> crear(@Valid @RequestBody MarcaRequest solicitud) {
		return ResponseEntity.status(HttpStatus.CREATED).body(marcaService.crear(solicitud));
	}

	@PutMapping("/{id}")
	public MarcaResponse actualizar(@PathVariable Long id, @Valid @RequestBody MarcaRequest solicitud) {
		return marcaService.actualizar(id, solicitud);
	}
}
