package cl.notara.ms_notas_metas;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.scheduling.annotation.EnableAsync;

@EnableAsync
@EnableFeignClients
@SpringBootApplication
public class MsNotasMetasApplication {

	public static void main(String[] args) {
		SpringApplication.run(MsNotasMetasApplication.class, args);
	}

}
