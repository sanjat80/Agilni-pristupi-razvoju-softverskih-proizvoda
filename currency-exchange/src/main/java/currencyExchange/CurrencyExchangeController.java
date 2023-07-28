package currencyExchange;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import io.github.resilience4j.ratelimiter.RequestNotPermitted;
import io.github.resilience4j.ratelimiter.annotation.RateLimiter;

@RestController
public class CurrencyExchangeController {
	
	@Autowired
	private CurrencyExchangeRepository repo;
	
	@Autowired 
	private Environment environment;

	@GetMapping("/currency-exchange/from/{from}/to/{to}")
	@RateLimiter(name = "default")
	public ResponseEntity<?> getExchange(@PathVariable String from, @PathVariable String to) {
		//return new CurrencyExchange(10000, from, to, BigDecimal.valueOf(117),"");
		String port = environment.getProperty("local.server.port");
		CurrencyExchange kurs = repo.findByFromAndToIgnoreCase(from, to);
		
		if(kurs!=null) {
			kurs.setEnvironment(port);
			return ResponseEntity.ok(kurs);
		}else {
			return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Zahtjeva razmjena valuta nije pronadjena!");
		}
		
	}
	
	@ExceptionHandler(RequestNotPermitted.class)
	public ResponseEntity<String> rateLimiterExceptionHandler(RequestNotPermitted ex){
		return ResponseEntity.status(503).body("Ovaj servis ima mogućnost obrade do 2 zahtjeva na svakih 30 sekundi!");
	}
	
	//localhost:8000/currency-exchange/from/EUR/to/RSD
}
