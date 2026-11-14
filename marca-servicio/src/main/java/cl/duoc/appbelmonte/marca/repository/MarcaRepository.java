package cl.duoc.appbelmonte.marca.repository;

import cl.duoc.appbelmonte.marca.model.Marca;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MarcaRepository extends JpaRepository<Marca, Long> {

	Optional<Marca> findByNombre(String nombre);

	boolean existsByNombre(String nombre);
}
