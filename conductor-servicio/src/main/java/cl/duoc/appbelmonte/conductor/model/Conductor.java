package cl.duoc.appbelmonte.conductor.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "conductores")
@Getter
@Setter
@NoArgsConstructor
public class Conductor {

	@Id
	private Long id;

	@Column(nullable = false, unique = true)
	private String numeroLicencia;

	@Column(nullable = false)
	private Boolean disponible = true;

	@Column(nullable = false)
	private Long vehiculoId;
}
