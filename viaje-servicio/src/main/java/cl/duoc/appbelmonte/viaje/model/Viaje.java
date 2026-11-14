package cl.duoc.appbelmonte.viaje.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "viajes")
@Getter
@Setter
@NoArgsConstructor
public class Viaje {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(nullable = false)
	private LocalDateTime fechaAsignacion;

	private LocalDateTime horaInicioReal;

	private LocalDateTime horaFinReal;

	@Column(nullable = false, unique = true)
	private Long solicitudId;

	@Column(nullable = false)
	private Long conductorId;

	@Column(nullable = false)
	private Long asignadoPorId;

	@Column(nullable = false)
	private Long estadoId;
}
