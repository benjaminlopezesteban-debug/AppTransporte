package cl.duoc.appbelmonte.conductor.repository;

import cl.duoc.appbelmonte.conductor.model.Conductor;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ConductorRepository extends JpaRepository<Conductor, Long> {

	List<Conductor> findAllByDisponibleTrue();
}
