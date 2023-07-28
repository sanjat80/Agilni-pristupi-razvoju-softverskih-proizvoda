package cryptoCurrency;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;
@EnableFeignClients
@SpringBootApplication
public class CryptoCurrencyApplication {

	public static void main(String[] args) {
		SpringApplication.run(CryptoCurrencyApplication.class, args);
	}

}
