package cl.duoc.appbelmonte.historial.model;

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
@Table(name = "historial_estados")
@Getter
@Setter
@NoArgsConstructor
public class HistorialEstado {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(nullable = false)
	private LocalDateTime fechaHora;

	private String detalle;

	@Column(nullable = false)
	private Long viajeId;

	private Long estadoAnteriorId;

	@Column(nullable = false)
	private Long estadoNuevoId;

	@Column(nullable = false)
	private Long cambiadoPorId;
}
