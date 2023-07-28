package cryptoWallet;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "bank-account")
public interface BankAccountProxy {
	@GetMapping("/bank-account/account/email/{email}")
	public String getAccountByEmail(@PathVariable String email);

}
