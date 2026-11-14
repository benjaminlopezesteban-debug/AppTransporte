package cl.duoc.appbelmonte.solicitud.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import cl.duoc.appbelmonte.solicitud.client.DireccionClient;
import cl.duoc.appbelmonte.solicitud.client.EstadoSolicitudClient;
import cl.duoc.appbelmonte.solicitud.client.PasajeroClient;
import cl.duoc.appbelmonte.solicitud.dto.DireccionDatos;
import cl.duoc.appbelmonte.solicitud.dto.DireccionResponse;
import cl.duoc.appbelmonte.solicitud.dto.EstadoSolicitudResponse;
import cl.duoc.appbelmonte.solicitud.dto.PasajeroDatos;
import cl.duoc.appbelmonte.solicitud.dto.PasajeroResponse;
import cl.duoc.appbelmonte.solicitud.dto.SolicitudRequest;
import cl.duoc.appbelmonte.solicitud.dto.SolicitudResponse;
import cl.duoc.appbelmonte.solicitud.exception.ReglaNegocioException;
import cl.duoc.appbelmonte.solicitud.model.SolicitudTraslado;
import cl.duoc.appbelmonte.solicitud.repository.SolicitudTrasladoRepository;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;

@ExtendWith(MockitoExtension.class)
class SolicitudServiceTest {

	@Mock
	private SolicitudTrasladoRepository solicitudRepository;

	@Mock
	private PasajeroClient pasajeroClient;

	@Mock
	private DireccionClient direccionClient;

	@Mock
	private EstadoSolicitudClient estadoSolicitudClient;

	@InjectMocks
	private SolicitudService solicitudService;

	@Test
	void crearGuardaSolicitudPendienteConLosIdsRemotos() {
		when(estadoSolicitudClient.obtener(1L))
				.thenReturn(new EstadoSolicitudResponse(1L, "PENDIENTE"));
		when(pasajeroClient.crear(any()))
				.thenReturn(new PasajeroResponse(10L, "Pasajero Prueba", "123"));
		when(direccionClient.crear(any()))
				.thenReturn(direccion(20L, "Origen"))
				.thenReturn(direccion(21L, "Destino"));
		when(solicitudRepository.save(any(SolicitudTraslado.class))).thenAnswer(invocacion -> {
			SolicitudTraslado solicitud = invocacion.getArgument(0);
			solicitud.setId(100L);
			return solicitud;
		});

		SolicitudResponse respuesta = solicitudService.crear(solicitudRequest(), 2L);

		assertThat(respuesta.id()).isEqualTo(100L);
		assertThat(respuesta.estadoId()).isEqualTo(1L);
		assertThat(respuesta.estadoNombre()).isEqualTo("PENDIENTE");
		assertThat(respuesta.solicitanteId()).isEqualTo(2L);
		assertThat(respuesta.pasajero().id()).isEqualTo(10L);
		assertThat(respuesta.origen().id()).isEqualTo(20L);
		assertThat(respuesta.destino().id()).isEqualTo(21L);

		ArgumentCaptor<SolicitudTraslado> captor = ArgumentCaptor
				.forClass(SolicitudTraslado.class);
		verify(solicitudRepository).save(captor.capture());
		SolicitudTraslado guardada = captor.getValue();
		assertThat(guardada.getEstadoId()).isEqualTo(1L);
		assertThat(guardada.getSolicitanteId()).isEqualTo(2L);
		assertThat(guardada.getPasajeroId()).isEqualTo(10L);
		assertThat(guardada.getOrigenId()).isEqualTo(20L);
		assertThat(guardada.getDestinoId()).isEqualTo(21L);
	}

	@Test
	void cancelarSolicitudPendienteDelDuenoCambiaEstado() {
		when(solicitudRepository.findById(1L))
				.thenReturn(Optional.of(solicitud(1L, 2L, 1L)));
		when(solicitudRepository.save(any(SolicitudTraslado.class)))
				.thenAnswer(invocacion -> invocacion.getArgument(0));
		stubRespuesta(3L, "CANCELADA");

		SolicitudResponse respuesta = solicitudService.cancelar(1L, 2L);

		assertThat(respuesta.estadoId()).isEqualTo(3L);
		assertThat(respuesta.estadoNombre()).isEqualTo("CANCELADA");
	}

	@Test
	void cancelarSolicitudAjenaLanzaAccesoDenegado() {
		when(solicitudRepository.findById(1L))
				.thenReturn(Optional.of(solicitud(1L, 2L, 1L)));

		assertThatThrownBy(() -> solicitudService.cancelar(1L, 3L))
				.isInstanceOf(AccessDeniedException.class);
		verify(solicitudRepository, never()).save(any());
	}

	@Test
	void cancelarSolicitudNoPendienteLanzaReglaDeNegocio() {
		when(solicitudRepository.findById(1L))
				.thenReturn(Optional.of(solicitud(1L, 2L, 2L)));

		assertThatThrownBy(() -> solicitudService.cancelar(1L, 2L))
				.isInstanceOf(ReglaNegocioException.class);
		verify(solicitudRepository, never()).save(any());
	}

	@Test
	void asignarSolicitudPendienteCambiaEstado() {
		when(solicitudRepository.findById(1L))
				.thenReturn(Optional.of(solicitud(1L, 2L, 1L)));
		when(solicitudRepository.save(any(SolicitudTraslado.class)))
				.thenAnswer(invocacion -> invocacion.getArgument(0));
		stubRespuesta(2L, "ASIGNADA");

		SolicitudResponse respuesta = solicitudService.asignar(1L);

		assertThat(respuesta.estadoId()).isEqualTo(2L);
		assertThat(respuesta.estadoNombre()).isEqualTo("ASIGNADA");
	}

	@Test
	void asignarSolicitudNoPendienteLanzaReglaDeNegocio() {
		when(solicitudRepository.findById(1L))
				.thenReturn(Optional.of(solicitud(1L, 2L, 3L)));

		assertThatThrownBy(() -> solicitudService.asignar(1L))
				.isInstanceOf(ReglaNegocioException.class);
	}

	@Test
	void obtenerSolicitudAjenaComoSolicitanteLanzaAccesoDenegado() {
		when(solicitudRepository.findById(1L))
				.thenReturn(Optional.of(solicitud(1L, 2L, 1L)));

		assertThatThrownBy(() -> solicitudService.obtener(1L, 3L, "SOLICITANTE"))
				.isInstanceOf(AccessDeniedException.class);
	}

	@Test
	void listarPorIdsFiltraPorFecha() {
		SolicitudTraslado enFecha = solicitud(1L, 2L, 1L);
		SolicitudTraslado otraFecha = solicitud(2L, 2L, 1L);
		otraFecha.setFecha(LocalDate.of(2026, 11, 13));
		when(solicitudRepository.findAllByIdIn(List.of(1L, 2L)))
				.thenReturn(List.of(enFecha, otraFecha));
		stubRespuesta(1L, "PENDIENTE");

		List<SolicitudResponse> respuestas = solicitudService
				.listarPorIds(List.of(1L, 2L), LocalDate.of(2026, 11, 12));

		assertThat(respuestas).hasSize(1);
		assertThat(respuestas.get(0).id()).isEqualTo(1L);
	}

	@Test
	void listarMiasDevuelveLasSolicitudesDelSolicitante() {
		when(solicitudRepository.findAllBySolicitanteId(2L))
				.thenReturn(List.of(solicitud(1L, 2L, 1L)));
		stubRespuesta(1L, "PENDIENTE");

		List<SolicitudResponse> respuestas = solicitudService.listarMias(2L);

		assertThat(respuestas).hasSize(1);
		assertThat(respuestas.get(0).solicitanteId()).isEqualTo(2L);
	}

	private SolicitudRequest solicitudRequest() {
		return new SolicitudRequest(LocalDate.of(2026, 11, 12), LocalTime.of(10, 30),
				"LA 999", "Prueba", new PasajeroDatos("Pasajero Prueba", "123"),
				new DireccionDatos("Calle Origen", -33.4489, -70.6693, 1L),
				new DireccionDatos("Calle Destino", -33.4314, -70.6093, 2L));
	}

	private SolicitudTraslado solicitud(Long id, Long solicitanteId, Long estadoId) {
		SolicitudTraslado solicitud = new SolicitudTraslado();
		solicitud.setId(id);
		solicitud.setSolicitanteId(solicitanteId);
		solicitud.setEstadoId(estadoId);
		solicitud.setPasajeroId(10L);
		solicitud.setOrigenId(20L);
		solicitud.setDestinoId(21L);
		solicitud.setFecha(LocalDate.of(2026, 11, 12));
		solicitud.setHora(LocalTime.of(10, 30));
		solicitud.setFechaCreacion(LocalDateTime.of(2026, 11, 10, 9, 0));
		return solicitud;
	}

	private DireccionResponse direccion(Long id, String calle) {
		return new DireccionResponse(id, calle, -33.4, -70.6, 1L, "Santiago", 1L,
				"Region Metropolitana");
	}

	private void stubRespuesta(Long estadoId, String estadoNombre) {
		when(estadoSolicitudClient.obtener(estadoId))
				.thenReturn(new EstadoSolicitudResponse(estadoId, estadoNombre));
		when(pasajeroClient.obtener(10L))
				.thenReturn(new PasajeroResponse(10L, "Pasajero Prueba", "123"));
		when(direccionClient.obtener(20L)).thenReturn(direccion(20L, "Origen"));
		when(direccionClient.obtener(21L)).thenReturn(direccion(21L, "Destino"));
	}
}
