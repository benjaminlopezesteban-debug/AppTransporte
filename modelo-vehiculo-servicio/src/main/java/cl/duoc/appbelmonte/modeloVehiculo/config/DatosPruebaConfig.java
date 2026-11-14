package cl.duoc.appbelmonte.modeloVehiculo.config;

import cl.duoc.appbelmonte.modeloVehiculo.model.ModeloVehiculo;
import cl.duoc.appbelmonte.modeloVehiculo.repository.ModeloVehiculoRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class DatosPruebaConfig {

	@Bean
	public CommandLineRunner cargarModelos(ModeloVehiculoRepository modeloVehiculoRepository) {
		return args -> {
			if (modeloVehiculoRepository.count() > 0) {
				return;
			}
			modeloVehiculoRepository.save(crear("Corolla", 1L));
			modeloVehiculoRepository.save(crear("Elantra", 2L));
			modeloVehiculoRepository.save(crear("Rio", 3L));
		};
	}

	private ModeloVehiculo crear(String nombre, Long marcaId) {
		ModeloVehiculo modelo = new ModeloVehiculo();
		modelo.setNombre(nombre);
		modelo.setMarcaId(marcaId);
		return modelo;
	}
}
