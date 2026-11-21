package cl.duoc.appbelmonte.modeloVehiculo;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication
@EnableFeignClients
public class ModeloVehiculoServicioApplication {

	public static void main(String[] args) {
		SpringApplication.run(ModeloVehiculoServicioApplication.class, args);
	}

}
