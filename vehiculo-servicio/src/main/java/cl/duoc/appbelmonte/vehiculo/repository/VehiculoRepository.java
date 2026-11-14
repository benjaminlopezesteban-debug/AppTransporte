package cl.duoc.appbelmonte.vehiculo.repository;

import cl.duoc.appbelmonte.vehiculo.model.Vehiculo;
import org.springframework.data.jpa.repository.JpaRepository;

public interface VehiculoRepository extends JpaRepository<Vehiculo, Long> {

	boolean existsByPatente(String patente);

	boolean existsByPatenteAndIdNot(String patente, Long id);
}
