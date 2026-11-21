package cl.duoc.appbelmonte.modeloVehiculo.client;

import cl.duoc.appbelmonte.modeloVehiculo.exception.DatosInvalidosException;
import cl.duoc.appbelmonte.modeloVehiculo.exception.ServicioNoDisponibleException;
import feign.FeignException;
import feign.RetryableException;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "marca-servicio")
public interface MarcaClient {

	@GetMapping("/api/marcas/{id}")
	MarcaResponse obtenerRemoto(@PathVariable("id") Long id);

	default MarcaResponse obtener(Long id) {
		try {
			MarcaResponse marca = obtenerRemoto(id);
			if (marca == null) {
				throw new ServicioNoDisponibleException("El servicio de marcas no está disponible");
			}
			return marca;
		} catch (FeignException.NotFound excepcion) {
			throw new DatosInvalidosException("La marca indicada no existe");
		} catch (RetryableException excepcion) {
			throw new ServicioNoDisponibleException("El servicio de marcas no está disponible");
		} catch (FeignException excepcion) {
			throw new ServicioNoDisponibleException("El servicio de marcas no está disponible");
		}
	}
}
