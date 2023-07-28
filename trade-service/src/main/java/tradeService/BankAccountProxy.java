package tradeService;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient("bank-account")
public interface BankAccountProxy {
	@GetMapping("/bank-account/account/{email}/{currency}")
	public double getCurrentAmount(@PathVariable String email, @PathVariable String currency);
	@PutMapping("/bank-account/amount/{email}/{currency}")
	public ResponseEntity<String> updateAmount(@PathVariable String email, @PathVariable String currency, @RequestParam double newAmount);
	@GetMapping("/bank-account/email/{email}")
	public BankAccountDto getAccountByEmail(@PathVariable String email);
}