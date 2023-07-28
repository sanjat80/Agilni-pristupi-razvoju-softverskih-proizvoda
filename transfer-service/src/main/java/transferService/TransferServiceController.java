package transferService;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import io.github.resilience4j.ratelimiter.RequestNotPermitted;
import io.github.resilience4j.ratelimiter.annotation.RateLimiter;

@RestController
public class TransferServiceController {
	
	@Autowired
	private UsersServiceProxy proxyUser;
	
	@Autowired
	private BankAccountProxy proxyBank;
	
	@Autowired
	private CryptoWalletProxy proxyWallet;
	
	@GetMapping("/transfer-service")
	@RateLimiter(name="default")
	public ResponseEntity<String> transferAmount(@RequestParam String fromUser, @RequestParam String toUser, @RequestParam Double quantity, @RequestParam String currency,@RequestHeader("Authorization") String authorizationHeader)
	{
		String currentRole = proxyUser.getCurrentRole(authorizationHeader);
		if(currentRole.equals("USER"))
		{
			List<String> emails = proxyBank.getAllBankAccountEmails();
			if (emails.contains(fromUser) && emails.contains(toUser)) {
	            if(isFiatCurrency(currency) && !isCryptoCurrency(currency))
	            {
	            	Double naknada = 0.01*quantity;
	                Double fiatFromAmount = proxyBank.getCurrentAmount(fromUser, currency);
	                if (fiatFromAmount == null) {
	                    String msg = "Valuta nije zavedena u novčaniku.";
	                    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(msg);
	                }
	                Double fiatToAmount = proxyBank.getCurrentAmount(toUser, currency);
	                Long toBankAccountId = proxyBank.getAccountIdByEmail(toUser);
	                if(fiatToAmount == null)
	                {
	                	 proxyBank.addCurrencyToAccount(toBankAccountId, currency);
	                	 fiatToAmount = 0.0;
	                }
	                Double totalAmount = naknada+quantity;
	                if(fiatFromAmount >= totalAmount)
	                {
	                	Double fromUserAccountAmount = fiatFromAmount - totalAmount;
	                	proxyBank.updateAmount(fromUser, currency, fromUserAccountAmount);
	                	Double toUserAccountAmount = fiatToAmount + quantity;
	                	proxyBank.updateAmount(toUser, currency, toUserAccountAmount);
	                	String message =String.format("Uspjesno je izvrsen transfer novca sa racuna zavedenim pod mejlom: %s, u iznosu od %.2f %s, na racun primaoca"
	                			+ " zaveden pod mejlom %s.", fromUser,quantity, currency, toUser);
	                	return ResponseEntity.status(HttpStatus.OK).body(message);
	                } else {
	                	String msg = "Nemate dovoljno novca na racunu za transfer.";
	                	return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(msg);
	                }
	            	
	            } else if (isCryptoCurrency(currency) && !isFiatCurrency(currency))
	            {
	            	Double naknada = 0.01*quantity;
	                Double cryptoFromAmount = proxyWallet.getCurrentAmount(fromUser, currency);
	                if (cryptoFromAmount == null) {
	                    String msg = "Valuta nije zavedena u novčaniku.";
	                    return ResponseEntity.status(HttpStatus.NOT_FOUND).body(msg);
	                }
	                Double cryptoToAmount = proxyWallet.getCurrentAmount(toUser, currency);
	                Long toAccountId = proxyWallet.getAccountIdByEmail(toUser);
	                if(cryptoToAmount == null)
	                {
	                	proxyWallet.addCurrencyToAccount(toAccountId, currency);
	                	cryptoToAmount = 0.0;
	                }
	                Double totalAmount = naknada+quantity;
	                if(cryptoFromAmount >= totalAmount)
	                {
	                	Double fromUserAccountAmount = cryptoFromAmount - totalAmount;
	                	proxyWallet.updateCryptoWallet(fromUser, currency, fromUserAccountAmount);
	                	Double toUserAccountAmount = cryptoToAmount + quantity;
	                	proxyWallet.updateCryptoWallet(toUser, currency, toUserAccountAmount);
	                	String message =String.format("Uspjesno je izvrsen transfer novca sa racuna zavedenim pod mejlom: %s, u iznosu od %.2f %s, na racun primaoca"
	                			+ " zaveden pod mejlom %s.", fromUser,quantity, currency, toUser);
	                	return ResponseEntity.status(HttpStatus.OK).body(message);
	                } else {
	                	String msg = "Nemate dovoljno novca na racunu za transfer.";
	                	return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(msg);
	                }
	            }else {
	            	String msg = "Valuta koju zelite da prebacite nije podrzana u sistemu.";
	            	return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(msg);
	            }
	        } else {
	            String msg = "Navedeni racuni izmedju kojih zelite da izvrsite transfer ne postoje u sistemu.";
            	return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(msg);
	        }
		}else {
			String msg = "Niste autorizovani da obavite ovu akciju.";
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
	
	@ExceptionHandler(RequestNotPermitted.class)
	public ResponseEntity<String> rateLimiterExceptionHandler(RequestNotPermitted ex){
		return ResponseEntity.status(503).body("Ovaj servis ima mogućnost obrade do 2 zahtjeva na svakih 45 sekundi!");
	}

}
