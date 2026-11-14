package cl.duoc.appbelmonte.vehiculo.config;

import cl.duoc.appbelmonte.vehiculo.model.Vehiculo;
import cl.duoc.appbelmonte.vehiculo.repository.VehiculoRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class DatosPruebaConfig {

	@Bean
	public CommandLineRunner cargarVehiculos(VehiculoRepository vehiculoRepository) {
		return args -> {
			if (vehiculoRepository.count() > 0) {
				return;
			}
			vehiculoRepository.save(crear("ABCD12", 2022, 1L));
			vehiculoRepository.save(crear("EFGH34", 2023, 2L));
			vehiculoRepository.save(crear("IJKL56", 2021, 3L));
		};
	}

	private Vehiculo crear(String patente, Integer anio, Long modeloId) {
		Vehiculo vehiculo = new Vehiculo();
		vehiculo.setPatente(patente);
		vehiculo.setAnio(anio);
		vehiculo.setModeloId(modeloId);
		return vehiculo;
	}
}
