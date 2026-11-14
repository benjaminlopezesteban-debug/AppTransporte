package cl.duoc.appbelmonte.pasajero.repository;

import cl.duoc.appbelmonte.pasajero.model.Pasajero;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PasajeroRepository extends JpaRepository<Pasajero, Long> {
}
