package cl.duoc.appbelmonte.direccion.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "direcciones")
@Getter
@Setter
@NoArgsConstructor
public class Direccion {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(nullable = false)
	private String calle;

	@Column(nullable = false)
	private Double latitud;

	@Column(nullable = false)
	private Double longitud;

	@Column(nullable = false)
	private Long comunaId;
}
