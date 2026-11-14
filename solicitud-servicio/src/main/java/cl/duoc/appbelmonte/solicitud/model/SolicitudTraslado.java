package cl.duoc.appbelmonte.solicitud.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "solicitudes_traslado")
@Getter
@Setter
@NoArgsConstructor
public class SolicitudTraslado {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(nullable = false)
	private LocalDate fecha;

	@Column(nullable = false)
	private LocalTime hora;

	private String numeroVuelo;

	private String observaciones;

	@Column(nullable = false)
	private LocalDateTime fechaCreacion;

	@Column(nullable = false)
	private Long solicitanteId;

	@Column(nullable = false)
	private Long pasajeroId;

	@Column(nullable = false)
	private Long origenId;

	@Column(nullable = false)
	private Long destinoId;

	@Column(nullable = false)
	private Long estadoId;
}
