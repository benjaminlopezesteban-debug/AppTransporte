package cl.duoc.appbelmonte.region.config;

import cl.duoc.appbelmonte.region.model.Region;
import cl.duoc.appbelmonte.region.repository.RegionRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class DatosPruebaConfig {

	@Bean
	public CommandLineRunner cargarRegiones(RegionRepository regionRepository) {
		return args -> {
			if (regionRepository.count() > 0) {
				return;
			}
			regionRepository.save(crear("Región Metropolitana"));
			regionRepository.save(crear("Región de Valparaíso"));
		};
	}

	private Region crear(String nombre) {
		Region region = new Region();
		region.setNombre(nombre);
		return region;
	}
}
