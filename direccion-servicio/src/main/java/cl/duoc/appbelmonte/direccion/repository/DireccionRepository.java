package cl.duoc.appbelmonte.direccion.repository;

import cl.duoc.appbelmonte.direccion.model.Direccion;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DireccionRepository extends JpaRepository<Direccion, Long> {
}
