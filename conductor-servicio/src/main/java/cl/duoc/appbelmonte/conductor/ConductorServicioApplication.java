package cl.duoc.appbelmonte.conductor;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication
@EnableFeignClients
public class ConductorServicioApplication {

	public static void main(String[] args) {
		SpringApplication.run(ConductorServicioApplication.class, args);
	}

}
