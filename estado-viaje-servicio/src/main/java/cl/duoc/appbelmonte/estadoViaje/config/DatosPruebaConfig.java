package cl.duoc.appbelmonte.estadoViaje.config;

import cl.duoc.appbelmonte.estadoViaje.model.EstadoViaje;
import cl.duoc.appbelmonte.estadoViaje.repository.EstadoViajeRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class DatosPruebaConfig {

	@Bean
	public CommandLineRunner cargarEstados(EstadoViajeRepository estadoViajeRepository) {
		return args -> {
			if (estadoViajeRepository.count() > 0) {
				return;
			}
			estadoViajeRepository.save(crear("ASIGNADO"));
			estadoViajeRepository.save(crear("EN_CURSO"));
			estadoViajeRepository.save(crear("FINALIZADO"));
		};
	}

	private EstadoViaje crear(String nombre) {
		EstadoViaje estado = new EstadoViaje();
		estado.setNombre(nombre);
		return estado;
	}
}
