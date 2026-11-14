package cl.duoc.appbelmonte.solicitud.config;

import cl.duoc.appbelmonte.solicitud.model.SolicitudTraslado;
import cl.duoc.appbelmonte.solicitud.repository.SolicitudTrasladoRepository;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class DatosPruebaConfig {

	@Bean
	public CommandLineRunner cargarSolicitudes(SolicitudTrasladoRepository solicitudRepository) {
		return args -> {
			if (solicitudRepository.count() > 0) {
				return;
			}
			solicitudRepository.save(crear(LocalDate.of(2026, 11, 10), LocalTime.of(9, 0),
					"LA 101", "Traslado al aeropuerto", 2L, 1L, 1L, 4L));
			solicitudRepository.save(crear(LocalDate.of(2026, 11, 10), LocalTime.of(14, 30),
					"LA 202", "Regreso del aeropuerto", 3L, 2L, 2L, 5L));
			solicitudRepository.save(crear(LocalDate.of(2026, 11, 11), LocalTime.of(8, 15),
					"LA 303", "Traslado temprano", 2L, 3L, 3L, 6L));
		};
	}

	private SolicitudTraslado crear(LocalDate fecha, LocalTime hora, String numeroVuelo,
			String observaciones, Long solicitanteId, Long pasajeroId, Long origenId,
			Long destinoId) {
		SolicitudTraslado solicitud = new SolicitudTraslado();
		solicitud.setFecha(fecha);
		solicitud.setHora(hora);
		solicitud.setNumeroVuelo(numeroVuelo);
		solicitud.setObservaciones(observaciones);
		solicitud.setFechaCreacion(LocalDateTime.now().withNano(0));
		solicitud.setSolicitanteId(solicitanteId);
		solicitud.setPasajeroId(pasajeroId);
		solicitud.setOrigenId(origenId);
		solicitud.setDestinoId(destinoId);
		solicitud.setEstadoId(1L);
		return solicitud;
	}
}
