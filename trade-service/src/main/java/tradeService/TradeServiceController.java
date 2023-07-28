package tradeService;
import java.io.Console;
import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import feign.FeignException;
import io.github.resilience4j.ratelimiter.RequestNotPermitted;
import io.github.resilience4j.ratelimiter.annotation.RateLimiter;



@RestController
public class TradeServiceController {
	@Autowired
	private TradeServiceRepository repository;
	
	@Autowired 
	private UserServiceProxy proxyUser;
	
	@Autowired
	private BankAccountProxy proxyBank;
	
	@Autowired
	private CryptoWalletProxy proxyWallet;
	private ObjectMapper objectMapper = new ObjectMapper();

	
	@GetMapping("/trade-service")
	@RateLimiter(name="default")
	public ResponseEntity<String> getTradeServicesFeign(@RequestParam String from, @RequestParam String to, @RequestParam Double quantity, @RequestHeader("Authorization") String authorizationHeader){
			String currentRole = proxyUser.getCurrentRole(authorizationHeader);
			String email = proxyUser.getUserEmail(authorizationHeader);
			//Long id = proxyUser.getUserId(authorizationHeader);
			if(currentRole.equals("USER")) {
				System.out.println(isCryptoCurrency(from) && isFiatCurrency(to));
					if (isFiatCurrency(from) && isCryptoCurrency(to)) {
						Double amount = 0.0;
			            if (!from.equals("EUR") && !from.equals("USD")) {
			                TradeService eurConversion = getExchange(from, "EUR");
			                Double eurKurs = eurConversion.getConversionMultiple();
			                Double eurAmount = quantity * eurKurs;
			                amount = eurAmount;
			                from = "EUR";
			            } else {
			                amount = quantity;
			            }
						 	TradeService response = getExchange(from, to);
			                Double kurs = response.getConversionMultiple();
			                Double cryptoAmount = amount * kurs;
			                System.out.println(cryptoAmount);
			                
			                Double fiatAmount = proxyBank.getCurrentAmount(email, from);
			                if(fiatAmount >= quantity) {
			                	   Double newFiatAmount = fiatAmount - quantity;
			                	   Double currentWallet = proxyWallet.getCurrentAmount(email, to);
			                	   Double totalAmount = currentWallet + cryptoAmount;
			                	   proxyBank.updateAmount(email, from, newFiatAmount);
			                	   proxyWallet.updateCryptoWallet(email, to, totalAmount);
			                	   			                	   
			                	   CryptoWalletDto wallet = proxyWallet.getCryptoWalletByEmail(email);
			                	   Double currentWalletAmount = proxyWallet.getCurrentAmount(email, to);
					                System.out.println(currentWalletAmount);

			                	   String message = String.format("Uspešno je izvršena razmena %.2f %s za %s. Trenutno stanje kripto novčanika: %.2f %s",
			                               quantity, from, to, currentWalletAmount, to);

			                       Map<String, Object> responseMap = new HashMap<>();
			                       responseMap.put("message", message);
			                       return ResponseEntity.ok(message);
			                   } else {
			                       String message = "Nemate dovoljan iznos ove valute na računu za razmjenu.";
			                       return ResponseEntity.badRequest().body(message);
			                   }
					} else if (isCryptoCurrency(from) && isFiatCurrency(to)) {
						Double amount = 0.0;
			            if (!to.equals("EUR") && !to.equals("USD")) {
			                TradeService eurConversion = getExchange(from, "EUR");
			                Double eurKurs = eurConversion.getConversionMultiple();
			                Double eurAmount = quantity * eurKurs;
			                amount = eurAmount;
			                to = "EUR";
			            } else {
			                amount = quantity;
			            }
		                TradeService response = getExchange(from, to);
		                Double kurs = response.getConversionMultiple();
		                Double fiatAmountReceived = amount * kurs;
		                System.out.println(fiatAmountReceived);
		                Double cryptoAmount = proxyWallet.getCurrentAmount(email, from);
		                if (cryptoAmount >= quantity) {
		                    Double newCryptoAmount = cryptoAmount - quantity;
		                    proxyWallet.updateCryptoWallet(email, from, newCryptoAmount);
		                    Double newFiatAmount = proxyBank.getCurrentAmount(email, to) + fiatAmountReceived;
		                    proxyBank.updateAmount(email, to, newFiatAmount);
		                    
		                    BankAccountDto bankAccountState = proxyBank.getAccountByEmail(email);
		                    Double currentBankAmount = proxyBank.getCurrentAmount(email, to);
		                    System.out.println(currentBankAmount);
		                    String message = String.format("Uspešno je izvršena razmena %.2f %s za %s. Trenutno stanje bankovnog računa: %.2f %s",
		                            quantity, from, to, currentBankAmount, to);

		                    Map<String, Object> responseMap = new HashMap<>();
		                       responseMap.put("message", message);
		                    try {
		                    	String responseString = objectMapper.writeValueAsString(responseMap);
		                           return ResponseEntity.ok(responseString);
		                       } catch (JsonProcessingException e) {
		                           String errorMessage = "Greška pri pretvaranju odgovora u JSON format.";
		                           return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorMessage);
		                       }
		                } else {
		                    String message = "Nemate dovoljan iznos ove valute na računu za razmjenu.";
		                    return ResponseEntity.badRequest().body(message);
		                }
		            }else {
		            	String message ="Proslijedjena kombinacija valuta nije dozvoljena.";
	                    return ResponseEntity.badRequest().body(message);
		            }
		}
		else {
				String msg = "Niste autorizovani da obavite ovu akciju";
				return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(msg);
		}
	}
	private boolean isFiatCurrency(String currency) {
	    return currency.equals("USD") || currency.equals("EUR") || currency.equals("GBP") 
	|| currency.equals("CHF") || currency.equals("RSD");
	}
	
	private boolean isCryptoCurrency(String currency) {
		return currency.equals("ETH") || currency.equals("BNB") || currency.equals("BTC");
	}
	
	@GetMapping("/trade-service/exchange/{from}/{to}")
	public TradeService getExchange(@PathVariable String from, @PathVariable String to) {
		//return new CurrencyExchange(10000, from, to, BigDecimal.valueOf(117),"");
		TradeService kurs = repository.findByFromAndToIgnoreCase(from, to);
		if(kurs!=null) {
			return kurs;
		}else {
			return null;
		}
		
	}
	

	private String generateTransactionReport(String fromCurrency, String toCurrency, double exchangedQuantity, double receivedQuantity) {
	    String transactionReport = "Izvještaj transakcije:\n" +
	            "IZ valute: " + fromCurrency + "\n" +
	            "U valutu: " + toCurrency + "\n" +
	            "Razmjenjena količina: " + exchangedQuantity + "\n" +
	            "Dobijena količina: " + receivedQuantity;
	    return transactionReport;
	}

	@ExceptionHandler(RequestNotPermitted.class)
		public ResponseEntity<String> rateLimiterExceptionHandler(RequestNotPermitted ex){
		return ResponseEntity.status(503).body("Ovaj servis ima mogućnost obrade do 2 zahtjeva na svakih 45 sekundi!");
	}


}
