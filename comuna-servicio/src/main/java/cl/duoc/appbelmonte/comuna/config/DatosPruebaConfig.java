package cl.duoc.appbelmonte.comuna.config;

import cl.duoc.appbelmonte.comuna.model.Comuna;
import cl.duoc.appbelmonte.comuna.repository.ComunaRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class DatosPruebaConfig {

	@Bean
	public CommandLineRunner cargarComunas(ComunaRepository comunaRepository) {
		return args -> {
			if (comunaRepository.count() > 0) {
				return;
			}
			comunaRepository.save(crear("Santiago", 1L));
			comunaRepository.save(crear("Providencia", 1L));
			comunaRepository.save(crear("Las Condes", 1L));
			comunaRepository.save(crear("Maipú", 1L));
			comunaRepository.save(crear("Pudahuel", 1L));
			comunaRepository.save(crear("Viña del Mar", 2L));
		};
	}

	private Comuna crear(String nombre, Long regionId) {
		Comuna comuna = new Comuna();
		comuna.setNombre(nombre);
		comuna.setRegionId(regionId);
		return comuna;
	}
}
