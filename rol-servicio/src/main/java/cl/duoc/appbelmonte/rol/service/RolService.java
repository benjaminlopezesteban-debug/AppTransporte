package cl.duoc.appbelmonte.rol.service;

import cl.duoc.appbelmonte.rol.dto.RolRequest;
import cl.duoc.appbelmonte.rol.dto.RolResponse;
import cl.duoc.appbelmonte.rol.exception.RecursoNoEncontradoException;
import cl.duoc.appbelmonte.rol.exception.ReglaNegocioException;
import cl.duoc.appbelmonte.rol.model.Rol;
import cl.duoc.appbelmonte.rol.repository.RolRepository;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class RolService {

	private final RolRepository rolRepository;

	public RolService(RolRepository rolRepository) {
		this.rolRepository = rolRepository;
	}

	@Transactional(readOnly = true)
	public List<RolResponse> listar() {
		return rolRepository.findAll().stream().map(this::aResponse).toList();
	}

	@Transactional(readOnly = true)
	public RolResponse obtener(Long id) {
		return aResponse(buscar(id));
	}

	@Transactional(readOnly = true)
	public RolResponse obtenerPorNombre(String nombre) {
		Rol rol = rolRepository.findByNombre(nombre)
				.orElseThrow(() -> new RecursoNoEncontradoException("El rol no existe"));
		return aResponse(rol);
	}

	@Transactional
	public RolResponse crear(RolRequest solicitud) {
		if (rolRepository.existsByNombre(solicitud.nombre())) {
			throw new ReglaNegocioException("Ya existe un rol con ese nombre");
		}
		Rol rol = new Rol();
		rol.setNombre(solicitud.nombre());
		return aResponse(rolRepository.save(rol));
	}

	@Transactional
	public RolResponse actualizar(Long id, RolRequest solicitud) {
		Rol rol = buscar(id);
		if (!rol.getNombre().equals(solicitud.nombre())
				&& rolRepository.existsByNombre(solicitud.nombre())) {
			throw new ReglaNegocioException("Ya existe un rol con ese nombre");
		}
		rol.setNombre(solicitud.nombre());
		return aResponse(rolRepository.save(rol));
	}

	private Rol buscar(Long id) {
		return rolRepository.findById(id)
				.orElseThrow(() -> new RecursoNoEncontradoException("El rol no existe"));
	}

	private RolResponse aResponse(Rol rol) {
		return new RolResponse(rol.getId(), rol.getNombre());
	}
}
