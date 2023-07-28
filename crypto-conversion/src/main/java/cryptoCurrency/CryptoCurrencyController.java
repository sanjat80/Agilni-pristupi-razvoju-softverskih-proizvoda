package cryptoCurrency;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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
public class CryptoCurrencyController {
	
	@Autowired
	private CryptoExchangeProxy proxy;
	
	@Autowired
	private UserServiceProxy proxyUser;
	
	@Autowired
	private CryptoWalletProxy proxyWallet;
	
	@GetMapping("/crypto-conversion/from/{from}/to/{to}/quantity/{quantity}")
	@RateLimiter(name="default")
	public CryptoConversion getConversion
		(@PathVariable String from, @PathVariable String to, @PathVariable double quantity) {
		
		HashMap<String,String> uriVariables = new HashMap<String,String>();
		uriVariables.put("from", from);
		uriVariables.put("to", to);
		
		ResponseEntity<CryptoConversion> response = 
				new RestTemplate().
				getForEntity("http://localhost:8400/crypto-exchange/from/{from}/to/{to}",
						CryptoConversion.class, uriVariables);
		
		CryptoConversion cc = response.getBody();
		
		return new CryptoConversion(from,to,cc.getConversionMultiple(), cc.getEnvironment(), quantity,
				cc.getConversionMultiple().multiply(BigDecimal.valueOf(quantity)));
	}
	
	/*@GetMapping("/crypto-conversion")
	public ResponseEntity<?> getConversionParams(@RequestParam String from, @RequestParam String to, @RequestParam double quantity) {
		
		HashMap<String,String> uriVariable = new HashMap<String, String>();
		uriVariable.put("from", from);
		uriVariable.put("to", to);
		
		try {
		ResponseEntity<CryptoConversion> response = new RestTemplate().
				getForEntity("http://localhost:8400/crypto-exchange/from/{from}/to/{to}", CryptoConversion.class, uriVariable);
		CryptoConversion responseBody = response.getBody();
		return ResponseEntity.status(HttpStatus.OK).body(new CryptoConversion(from,to,responseBody.getConversionMultiple(),responseBody.getEnvironment(),
				quantity, responseBody.getConversionMultiple().multiply(BigDecimal.valueOf(quantity))));
		}
		catch(HttpClientErrorException e) {
			return ResponseEntity.status(e.getStatusCode()).body(e.getMessage());
		}
	}*/
	
	@GetMapping("/crypto-conversion")
	@RateLimiter(name="default")
	public ResponseEntity<?> getConversionFeign(@RequestParam String from, @RequestParam String to, @RequestParam double quantity, @RequestHeader("Authorization") String authorizationHeader){
		
		try {
			String currentRole = proxyUser.getCurrentUser(authorizationHeader);
			String email = proxyUser.getEmailOfCurrentUser(authorizationHeader);
			Long id = proxyUser.extractUsersId(email);
			Long walletId=proxyWallet.getAccountIdByEmail(email);
			if(currentRole.equals("USER"))
			{
				double amount = proxyWallet.getCurrentAmount(email, from);
				if(amount >= quantity)
				{
					ResponseEntity<CryptoConversion> response = proxy.getExchange(from, to);
					CryptoConversion responseBody = response.getBody();
					double newAmount = amount - quantity;
					proxyWallet.updateAmount(walletId,from, newAmount);
					CryptoWalletDto cryptoWalletState = proxyWallet.getCryptoWalletByEmail(email);
					/*return ResponseEntity.ok(new CurrencyConversion(from,to,responseBody.getConversionMultiple(),responseBody.getEnvironment()+" feign",
						quantity, responseBody.getConversionMultiple().multiply(BigDecimal.valueOf(quantity))));*/
					String message = String.format("Uspešno je izvršena razmena %.2f %s za %s. Trenutno stanje novcanika: %.2f %s",
						    quantity, from, to, amount, from);

						Map<String, Object> responseMap = new HashMap<>();
						responseMap.put("message", message);
						responseMap.put("cryptoWalletState", cryptoWalletState);

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
	
	@ExceptionHandler(RequestNotPermitted.class)
	public ResponseEntity<String> rateLimiterExceptionHandler(RequestNotPermitted ex){
		return ResponseEntity.status(503).body("Ovaj servis ima mogućnost obrade do 2 zahtjeva na svakih 45 sekundi!");
	}
	
	

}
