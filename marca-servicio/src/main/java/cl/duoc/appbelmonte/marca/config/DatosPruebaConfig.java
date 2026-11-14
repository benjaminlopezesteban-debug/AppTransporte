package cl.duoc.appbelmonte.marca.config;

import cl.duoc.appbelmonte.marca.model.Marca;
import cl.duoc.appbelmonte.marca.repository.MarcaRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class DatosPruebaConfig {

	@Bean
	public CommandLineRunner cargarMarcas(MarcaRepository marcaRepository) {
		return args -> {
			if (marcaRepository.count() > 0) {
				return;
			}
			marcaRepository.save(crear("Toyota"));
			marcaRepository.save(crear("Hyundai"));
			marcaRepository.save(crear("Kia"));
		};
	}

	private Marca crear(String nombre) {
		Marca marca = new Marca();
		marca.setNombre(nombre);
		return marca;
	}
}
