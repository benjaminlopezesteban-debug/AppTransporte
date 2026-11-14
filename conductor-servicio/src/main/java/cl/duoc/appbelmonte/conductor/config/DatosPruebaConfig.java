package cl.duoc.appbelmonte.conductor.config;

import cl.duoc.appbelmonte.conductor.model.Conductor;
import cl.duoc.appbelmonte.conductor.repository.ConductorRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class DatosPruebaConfig {

	@Bean
	public CommandLineRunner cargarConductores(ConductorRepository conductorRepository) {
		return args -> {
			if (conductorRepository.count() > 0) {
				return;
			}
			conductorRepository.save(crear(4L, "LIC-0004", 1L));
			conductorRepository.save(crear(5L, "LIC-0005", 2L));
			conductorRepository.save(crear(6L, "LIC-0006", 3L));
		};
	}

	private Conductor crear(Long id, String numeroLicencia, Long vehiculoId) {
		Conductor conductor = new Conductor();
		conductor.setId(id);
		conductor.setNumeroLicencia(numeroLicencia);
		conductor.setDisponible(true);
		conductor.setVehiculoId(vehiculoId);
		return conductor;
	}
}
