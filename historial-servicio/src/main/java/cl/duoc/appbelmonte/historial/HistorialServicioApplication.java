package cl.duoc.appbelmonte.historial;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication
@EnableFeignClients
public class HistorialServicioApplication {

	public static void main(String[] args) {
		SpringApplication.run(HistorialServicioApplication.class, args);
	}

}
