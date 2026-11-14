package cl.duoc.appbelmonte.estadoSolicitud.config;

import cl.duoc.appbelmonte.estadoSolicitud.model.EstadoSolicitud;
import cl.duoc.appbelmonte.estadoSolicitud.repository.EstadoSolicitudRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class DatosPruebaConfig {

	@Bean
	public CommandLineRunner cargarEstados(EstadoSolicitudRepository estadoSolicitudRepository) {
		return args -> {
			if (estadoSolicitudRepository.count() > 0) {
				return;
			}
			estadoSolicitudRepository.save(crear("PENDIENTE"));
			estadoSolicitudRepository.save(crear("ASIGNADA"));
			estadoSolicitudRepository.save(crear("CANCELADA"));
		};
	}

	private EstadoSolicitud crear(String nombre) {
		EstadoSolicitud estado = new EstadoSolicitud();
		estado.setNombre(nombre);
		return estado;
	}
}
