package cl.duoc.appbelmonte.comuna;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication
@EnableFeignClients
public class ComunaServicioApplication {

	public static void main(String[] args) {
		SpringApplication.run(ComunaServicioApplication.class, args);
	}

}
