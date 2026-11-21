package cl.duoc.appbelmonte.vehiculo;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication
@EnableFeignClients
public class VehiculoServicioApplication {

	public static void main(String[] args) {
		SpringApplication.run(VehiculoServicioApplication.class, args);
	}

}
