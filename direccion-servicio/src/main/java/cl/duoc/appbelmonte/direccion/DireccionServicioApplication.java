package cl.duoc.appbelmonte.direccion;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication
@EnableFeignClients
public class DireccionServicioApplication {

	public static void main(String[] args) {
		SpringApplication.run(DireccionServicioApplication.class, args);
	}

}
