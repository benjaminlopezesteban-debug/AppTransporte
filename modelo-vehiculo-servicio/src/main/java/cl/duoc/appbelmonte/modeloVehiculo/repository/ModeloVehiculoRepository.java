package cl.duoc.appbelmonte.modeloVehiculo.repository;

import cl.duoc.appbelmonte.modeloVehiculo.model.ModeloVehiculo;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ModeloVehiculoRepository extends JpaRepository<ModeloVehiculo, Long> {

	List<ModeloVehiculo> findAllByMarcaId(Long marcaId);
}
