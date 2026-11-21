package cl.duoc.appbelmonte.viaje.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import cl.duoc.appbelmonte.viaje.client.ConductorClient;
import cl.duoc.appbelmonte.viaje.client.EstadoViajeClient;
import cl.duoc.appbelmonte.viaje.client.HistorialClient;
import cl.duoc.appbelmonte.viaje.client.SolicitudClient;
import cl.duoc.appbelmonte.viaje.dto.AsignacionRequest;
import cl.duoc.appbelmonte.viaje.dto.ConductorResponse;
import cl.duoc.appbelmonte.viaje.dto.DireccionResponse;
import cl.duoc.appbelmonte.viaje.dto.EstadoViajeResponse;
import cl.duoc.appbelmonte.viaje.dto.PasajeroResponse;
import cl.duoc.appbelmonte.viaje.dto.ReasignacionRequest;
import cl.duoc.appbelmonte.viaje.dto.SolicitudResponse;
import cl.duoc.appbelmonte.viaje.dto.ViajeResponse;
import cl.duoc.appbelmonte.viaje.exception.ReglaNegocioException;
import cl.duoc.appbelmonte.viaje.exception.ServicioNoDisponibleException;
import cl.duoc.appbelmonte.viaje.model.Viaje;
import cl.duoc.appbelmonte.viaje.repository.ViajeRepository;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ViajeServiceTest {

	@Mock
	private ViajeRepository viajeRepository;

	@Mock
	private SolicitudClient solicitudClient;

	@Mock
	private ConductorClient conductorClient;

	@Mock
	private EstadoViajeClient estadoViajeClient;

	@Mock
	private HistorialClient historialClient;

	@InjectMocks
	private ViajeService viajeService;

	@Test
	void asignarGuardaViajeYAsignaLaSolicitud() {
		when(solicitudClient.obtener(1L)).thenReturn(solicitud(1L, "PENDIENTE", 2L));
		when(viajeRepository.existsBySolicitudId(1L)).thenReturn(false);
		when(conductorClient.obtener(4L)).thenReturn(conductor(4L, true));
		when(viajeRepository.save(any(Viaje.class))).thenAnswer(invocacion -> {
			Viaje viaje = invocacion.getArgument(0);
			viaje.setId(10L);
			return viaje;
		});
		when(solicitudClient.asignar(1L)).thenReturn(solicitud(2L, "ASIGNADA", 2L));
		when(estadoViajeClient.obtener(1L))
				.thenReturn(new EstadoViajeResponse(1L, "ASIGNADO"));

		ViajeResponse respuesta = viajeService.asignar(new AsignacionRequest(1L, 4L, 1L), 1L);

		assertThat(respuesta.id()).isEqualTo(10L);
		assertThat(respuesta.estadoId()).isEqualTo(1L);
		assertThat(respuesta.estadoNombre()).isEqualTo("ASIGNADO");
		assertThat(respuesta.conductorId()).isEqualTo(4L);
		assertThat(respuesta.asignadoPorId()).isEqualTo(1L);
		assertThat(respuesta.solicitud().estadoNombre()).isEqualTo("ASIGNADA");
		verify(historialClient).registrar(any());
	}

	@Test
	void asignarSolicitudNoPendienteLanzaReglaDeNegocio() {
		when(solicitudClient.obtener(1L)).thenReturn(solicitud(2L, "ASIGNADA", 2L));

		assertThatThrownBy(() -> viajeService.asignar(new AsignacionRequest(1L, 4L, 1L), 1L))
				.isInstanceOf(ReglaNegocioException.class);
		verify(viajeRepository, never()).save(any());
		verify(solicitudClient, never()).asignar(any());
	}

	@Test
	void asignarConductorNoDisponibleLanzaReglaDeNegocio() {
		when(solicitudClient.obtener(1L)).thenReturn(solicitud(1L, "PENDIENTE", 2L));
		when(viajeRepository.existsBySolicitudId(1L)).thenReturn(false);
		when(conductorClient.obtener(4L)).thenReturn(conductor(4L, false));

		assertThatThrownBy(() -> viajeService.asignar(new AsignacionRequest(1L, 4L, 1L), 1L))
				.isInstanceOf(ReglaNegocioException.class);
		verify(viajeRepository, never()).save(any());
	}

	@Test
	void asignarSolicitudConViajeExistenteLanzaReglaDeNegocio() {
		when(solicitudClient.obtener(1L)).thenReturn(solicitud(1L, "PENDIENTE", 2L));
		when(viajeRepository.existsBySolicitudId(1L)).thenReturn(true);

		assertThatThrownBy(() -> viajeService.asignar(new AsignacionRequest(1L, 4L, 1L), 1L))
				.isInstanceOf(ReglaNegocioException.class);
		verify(viajeRepository, never()).save(any());
	}

	@Test
	void asignarPropagaFalloDelServicioDeSolicitudes() {
		when(solicitudClient.obtener(1L)).thenReturn(solicitud(1L, "PENDIENTE", 2L));
		when(viajeRepository.existsBySolicitudId(1L)).thenReturn(false);
		when(conductorClient.obtener(4L)).thenReturn(conductor(4L, true));
		when(viajeRepository.save(any(Viaje.class)))
				.thenAnswer(invocacion -> invocacion.getArgument(0));
		when(solicitudClient.asignar(1L))
				.thenThrow(new ServicioNoDisponibleException("solicitud caida"));

		assertThatThrownBy(() -> viajeService.asignar(new AsignacionRequest(1L, 4L, 1L), 1L))
				.isInstanceOf(ServicioNoDisponibleException.class);
		verify(viajeRepository).save(any(Viaje.class));
	}

	@Test
	void reasignarSoloPermiteViajeAsignado() {
		when(viajeRepository.findById(10L)).thenReturn(Optional.of(viaje(10L, 4L, 2L)));

		assertThatThrownBy(() -> viajeService.reasignar(10L,
				new ReasignacionRequest(5L, 1L), 1L))
				.isInstanceOf(ReglaNegocioException.class);
		verify(viajeRepository, never()).save(any());
	}

	@Test
	void reasignarCambiaElConductorYRegistraHistorial() {
		when(viajeRepository.findById(10L)).thenReturn(Optional.of(viaje(10L, 4L, 1L)));
		when(conductorClient.obtener(5L)).thenReturn(conductor(5L, true));
		when(viajeRepository.save(any(Viaje.class)))
				.thenAnswer(invocacion -> invocacion.getArgument(0));
		when(solicitudClient.obtener(1L)).thenReturn(solicitud(2L, "ASIGNADA", 2L));
		when(estadoViajeClient.obtener(1L))
				.thenReturn(new EstadoViajeResponse(1L, "ASIGNADO"));

		ViajeResponse respuesta = viajeService.reasignar(10L,
				new ReasignacionRequest(5L, 1L), 1L);

		assertThat(respuesta.conductorId()).isEqualTo(5L);
		assertThat(respuesta.asignadoPorId()).isEqualTo(1L);
		verify(historialClient).registrar(any());
	}

	@Test
	void iniciarCambiaEstadoYRegistraHoraDeInicio() {
		when(viajeRepository.findById(10L)).thenReturn(Optional.of(viaje(10L, 4L, 1L)));
		when(viajeRepository.save(any(Viaje.class)))
				.thenAnswer(invocacion -> invocacion.getArgument(0));
		when(solicitudClient.obtener(1L)).thenReturn(solicitud(2L, "ASIGNADA", 2L));
		when(estadoViajeClient.obtener(2L))
				.thenReturn(new EstadoViajeResponse(2L, "EN_CURSO"));

		ViajeResponse respuesta = viajeService.iniciar(10L);

		assertThat(respuesta.estadoId()).isEqualTo(2L);
		assertThat(respuesta.estadoNombre()).isEqualTo("EN_CURSO");
		assertThat(respuesta.horaInicioReal()).isNotNull();
		verify(historialClient).registrar(any());
	}

	@Test
	void finalizarSoloPermiteViajeEnCurso() {
		when(viajeRepository.findById(10L)).thenReturn(Optional.of(viaje(10L, 4L, 1L)));

		assertThatThrownBy(() -> viajeService.finalizar(10L))
				.isInstanceOf(ReglaNegocioException.class);
		verify(viajeRepository, never()).save(any());
	}

	@Test
	void finalizarCambiaEstadoYRegistraHoraDeFin() {
		when(viajeRepository.findById(10L)).thenReturn(Optional.of(viaje(10L, 4L, 2L)));
		when(viajeRepository.save(any(Viaje.class)))
				.thenAnswer(invocacion -> invocacion.getArgument(0));
		when(solicitudClient.obtener(1L)).thenReturn(solicitud(2L, "ASIGNADA", 2L));
		when(estadoViajeClient.obtener(3L))
				.thenReturn(new EstadoViajeResponse(3L, "FINALIZADO"));

		ViajeResponse respuesta = viajeService.finalizar(10L);

		assertThat(respuesta.estadoId()).isEqualTo(3L);
		assertThat(respuesta.estadoNombre()).isEqualTo("FINALIZADO");
		assertThat(respuesta.horaFinReal()).isNotNull();
		verify(historialClient).registrar(any());
	}

	@Test
	void unFalloDelHistorialNoImpideIniciarElViaje() {
		when(viajeRepository.findById(10L)).thenReturn(Optional.of(viaje(10L, 4L, 1L)));
		when(viajeRepository.save(any(Viaje.class)))
				.thenAnswer(invocacion -> invocacion.getArgument(0));
		when(solicitudClient.obtener(1L)).thenReturn(solicitud(2L, "ASIGNADA", 2L));
		when(estadoViajeClient.obtener(2L))
				.thenReturn(new EstadoViajeResponse(2L, "EN_CURSO"));
		doThrow(new ServicioNoDisponibleException("historial caido"))
				.when(historialClient).registrar(any());

		ViajeResponse respuesta = viajeService.iniciar(10L);

		assertThat(respuesta.estadoId()).isEqualTo(2L);
	}

	@Test
	void listarMiosDevuelveLosViajesDelConductor() {
		when(viajeRepository.findAllByConductorId(4L))
				.thenReturn(List.of(viaje(10L, 4L, 1L)));
		when(solicitudClient.listarPorIds(List.of(1L)))
				.thenReturn(List.of(solicitud(2L, "ASIGNADA", 2L)));
		when(estadoViajeClient.obtener(1L))
				.thenReturn(new EstadoViajeResponse(1L, "ASIGNADO"));

		List<ViajeResponse> respuestas = viajeService.listarMios(4L);

		assertThat(respuestas).hasSize(1);
		assertThat(respuestas.get(0).conductorId()).isEqualTo(4L);
	}

	private Viaje viaje(Long id, Long conductorId, Long estadoId) {
		Viaje viaje = new Viaje();
		viaje.setId(id);
		viaje.setConductorId(conductorId);
		viaje.setEstadoId(estadoId);
		viaje.setSolicitudId(1L);
		viaje.setAsignadoPorId(1L);
		viaje.setFechaAsignacion(LocalDateTime.of(2026, 11, 10, 9, 0));
		return viaje;
	}

	private ConductorResponse conductor(Long id, boolean disponible) {
		return new ConductorResponse(id, "LIC-000" + id, disponible, 1L, "ABCD12",
				"Modelo", "Marca");
	}

	private SolicitudResponse solicitud(Long estadoId, String estadoNombre, Long solicitanteId) {
		return new SolicitudResponse(1L, LocalDate.of(2026, 11, 12), LocalTime.of(10, 30),
				"LA 999", "Prueba", LocalDateTime.of(2026, 11, 10, 9, 0), estadoId,
				estadoNombre, solicitanteId,
				new PasajeroResponse(10L, "Pasajero Prueba", "123"),
				new DireccionResponse(20L, "Origen", -33.4, -70.6, 1L, "Santiago", 1L,
						"Region Metropolitana"),
				new DireccionResponse(21L, "Destino", -33.5, -70.7, 2L, "Providencia", 1L,
						"Region Metropolitana"));
	}
}
