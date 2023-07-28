package currencyConversion;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.client.RestTemplate;

import feign.FeignException;
import io.github.resilience4j.ratelimiter.RequestNotPermitted;
import io.github.resilience4j.ratelimiter.annotation.RateLimiter;

@RestController
public class CurrencyConversionController {
	
	@Autowired
	private CurrencyExchangeProxy proxy;
	@Autowired
	private UserServiceProxy userProxy;
	@Autowired
	private BankAccountProxy bankProxy;

	//localhost:8100/currency-conversion/from/EUR/to/RSD/quantity/100
	@GetMapping("/currency-conversion/from/{from}/to/{to}/quantity/{quantity}")
	@RateLimiter(name="default")
	public CurrencyConversion getConversion
		(@PathVariable String from, @PathVariable String to, @PathVariable double quantity) {
		
		HashMap<String,String> uriVariables = new HashMap<String,String>();
		uriVariables.put("from", from);
		uriVariables.put("to", to);
		
		ResponseEntity<CurrencyConversion> response = 
				new RestTemplate().
				getForEntity("http://localhost:8000/currency-exchange/from/{from}/to/{to}",
						CurrencyConversion.class, uriVariables);
		
		CurrencyConversion cc = response.getBody();
		
		return new CurrencyConversion(from,to,cc.getConversionMultiple(), cc.getEnvironment(), quantity,
				cc.getConversionMultiple().multiply(BigDecimal.valueOf(quantity)));
	}
	
	//localhost:8100/currency-conversion?from=EUR&to=RSD&quantity=50
	/*@GetMapping("/currency-conversion")
	public ResponseEntity<?> getConversionParams(@RequestParam String from, @RequestParam String to, @RequestParam double quantity) {
		
		HashMap<String,String> uriVariable = new HashMap<String, String>();
		uriVariable.put("from", from);
		uriVariable.put("to", to);
		
		try {
		ResponseEntity<CurrencyConversion> response = new RestTemplate().
				getForEntity("http://localhost:8000/currency-exchange/from/{from}/to/{to}", CurrencyConversion.class, uriVariable);
		CurrencyConversion responseBody = response.getBody();
		return ResponseEntity.status(HttpStatus.OK).body(new CurrencyConversion(from,to,responseBody.getConversionMultiple(),responseBody.getEnvironment(),
				quantity, responseBody.getConversionMultiple().multiply(BigDecimal.valueOf(quantity))));
		}
		catch(HttpClientErrorException e) {
			return ResponseEntity.status(e.getStatusCode()).body(e.getMessage());
		}
	}*/
	
	//localhost:8100/currency-conversion-feign?from=EUR&to=RSD&quantity=50
	@GetMapping("/currency-conversion")
	@RateLimiter(name="default")
	public ResponseEntity<?> getConversionFeign(@RequestParam String from, @RequestParam String to, @RequestParam double quantity, @RequestHeader("Authorization") String authorizationHeaders){
		
		try {
			String currentRole = userProxy.getCurrentUser(authorizationHeaders);
			String email = userProxy.getEmailOfCurrentUser(authorizationHeaders);
			Long id = userProxy.extractId(email);
			if(currentRole.equals("USER"))
			{
				double amount = bankProxy.getCurrentAmountForCurrency(email, from);
				if(amount >= quantity)
				{
					ResponseEntity<CurrencyConversion> response = proxy.getExchange(from, to);
					CurrencyConversion responseBody = response.getBody();
					Double kurs = responseBody.getConversionMultiple().doubleValue();
					Double totalAmount = kurs*quantity;
					double newAmount = amount - quantity;
					bankProxy.updateAmount(email,from, newAmount);
					BankAccountDto bankAccountState = bankProxy.getAccountByEmail(email);
					/*return ResponseEntity.ok(new CurrencyConversion(from,to,responseBody.getConversionMultiple(),responseBody.getEnvironment()+" feign",
						quantity, responseBody.getConversionMultiple().multiply(BigDecimal.valueOf(quantity))));*/
					String message = String.format("Uspešno je izvršena razmena %.2f %s za %.2f %s. Trenutno stanje bankovnog računa: %.2f %s",
						    quantity, from,totalAmount, to, amount, from);

						Map<String, Object> responseMap = new HashMap<>();
						responseMap.put("message", message);
						responseMap.put("bankAccountState", bankAccountState);

						return ResponseEntity.ok(responseMap);
				}else
				{
					String message = "Nemate dovoljan iznos ove valute na racunu za razmjenu.";
					return ResponseEntity.badRequest().body(message);
				}
			}else
			{
				String message = "Samo korisnici sa ulogom USER mogu da pristupe ovom servisu.";
				return ResponseEntity.badRequest().body(message);
			}
			
		}catch(FeignException e) {
			return ResponseEntity.status(e.status()).body(e.getMessage());
		}
	}
	
	@ExceptionHandler(MissingServletRequestParameterException.class)
	public ResponseEntity<String> handleMissingParams(MissingServletRequestParameterException ex) {
	    String parameter = ex.getParameterName();
	    //return ResponseEntity.status(ex.getStatusCode()).body(ex.getMessage());
	    return ResponseEntity.status(ex.getStatusCode()).body("Value [" + ex.getParameterType() + "] of parameter [" + parameter + "] has been ommited");
	}
	
	@ExceptionHandler(RequestNotPermitted.class)
	public ResponseEntity<String> rateLimiterExceptionHandler(RequestNotPermitted ex){
		return ResponseEntity.status(503).body("Ovaj servis ima mogućnost obrade do 2 zahtjeva na svakih 45 sekundi!");
	}
	
	
}
