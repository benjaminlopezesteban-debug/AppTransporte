package cl.duoc.appbelmonte.rol.repository;

import cl.duoc.appbelmonte.rol.model.Rol;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RolRepository extends JpaRepository<Rol, Long> {

	Optional<Rol> findByNombre(String nombre);

	boolean existsByNombre(String nombre);
}
