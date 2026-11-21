package cl.duoc.appbelmonte.solicitud;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication
@EnableFeignClients
public class SolicitudServicioApplication {

	public static void main(String[] args) {
		SpringApplication.run(SolicitudServicioApplication.class, args);
	}

}
