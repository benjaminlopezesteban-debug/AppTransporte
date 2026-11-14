package cl.duoc.appbelmonte.direccion.config;

import cl.duoc.appbelmonte.direccion.model.Direccion;
import cl.duoc.appbelmonte.direccion.repository.DireccionRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class DatosPruebaConfig {

	@Bean
	public CommandLineRunner cargarDirecciones(DireccionRepository direccionRepository) {
		return args -> {
			if (direccionRepository.count() > 0) {
				return;
			}
			direccionRepository.save(crear("Av. Libertador Bernardo O'Higgins 123",
					-33.4489, -70.6693, 1L));
			direccionRepository.save(crear("Av. Providencia 456", -33.4314, -70.6093, 2L));
			direccionRepository.save(crear("Av. Apoquindo 789", -33.4167, -70.5833, 3L));
			direccionRepository.save(crear("Av. Pajaritos 101", -33.5167, -70.7667, 4L));
			direccionRepository.save(crear("Av. Aeropuerto 202", -33.3928, -70.7856, 5L));
			direccionRepository.save(crear("Av. Marina 303", -33.0245, -71.5518, 6L));
		};
	}

	private Direccion crear(String calle, Double latitud, Double longitud, Long comunaId) {
		Direccion direccion = new Direccion();
		direccion.setCalle(calle);
		direccion.setLatitud(latitud);
		direccion.setLongitud(longitud);
		direccion.setComunaId(comunaId);
		return direccion;
	}
}
