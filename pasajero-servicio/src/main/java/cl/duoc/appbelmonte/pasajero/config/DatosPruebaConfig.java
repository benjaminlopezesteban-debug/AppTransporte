package cl.duoc.appbelmonte.pasajero.config;

import cl.duoc.appbelmonte.pasajero.model.Pasajero;
import cl.duoc.appbelmonte.pasajero.repository.PasajeroRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class DatosPruebaConfig {

	@Bean
	public CommandLineRunner cargarPasajeros(PasajeroRepository pasajeroRepository) {
		return args -> {
			if (pasajeroRepository.count() > 0) {
				return;
			}
			pasajeroRepository.save(crear("Sofía Martínez", "+56977777777"));
			pasajeroRepository.save(crear("Jorge Ramírez", "+56988888888"));
			pasajeroRepository.save(crear("Valentina Pérez", "+56999999999"));
		};
	}

	private Pasajero crear(String nombre, String telefono) {
		Pasajero pasajero = new Pasajero();
		pasajero.setNombre(nombre);
		pasajero.setTelefono(telefono);
		return pasajero;
	}
}
