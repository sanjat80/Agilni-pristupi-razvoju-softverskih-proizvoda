package currencyConversion;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestParam;


@FeignClient(name = "bank-account")
public interface BankAccountProxy {
	@GetMapping("/bank-account/account/{email}/{currency}")
	public Double getCurrentAmountForCurrency(@PathVariable String email, @PathVariable String currency);
	@PutMapping("/bank-account/amount/{email}/{currency}")
	public ResponseEntity<String> updateAmount(@PathVariable String email, @PathVariable String currency, @RequestParam Double newAmount);
	@GetMapping("/bank-account/email/{email}")
	public BankAccountDto getAccountByEmail(@PathVariable String email);
}