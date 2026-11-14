package cl.duoc.appbelmonte.rol.config;

import cl.duoc.appbelmonte.rol.model.Rol;
import cl.duoc.appbelmonte.rol.repository.RolRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class DatosPruebaConfig {

	@Bean
	public CommandLineRunner cargarRoles(RolRepository rolRepository) {
		return args -> {
			if (rolRepository.count() > 0) {
				return;
			}
			rolRepository.save(crear("ADMINISTRADOR"));
			rolRepository.save(crear("SOLICITANTE"));
			rolRepository.save(crear("CONDUCTOR"));
		};
	}

	private Rol crear(String nombre) {
		Rol rol = new Rol();
		rol.setNombre(nombre);
		return rol;
	}
}
