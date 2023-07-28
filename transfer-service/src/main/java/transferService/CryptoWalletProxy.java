package transferService;

import java.util.List;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient("crypto-wallet")
public interface CryptoWalletProxy {
	@PutMapping("/crypto-wallet/amount/{email}/{currency}")
	public ResponseEntity<String> updateCryptoWallet(@PathVariable String email, @PathVariable String currency, @RequestParam double amount);
	@GetMapping("/crypto-wallet/wallet/email/{email}")
	public CryptoWalletDto getCryptoWalletByEmail(@PathVariable String email);
	@GetMapping("/crypto-wallet/wallet/{email}/{crypto}")
	public Double getCurrentAmount(@PathVariable String email, @PathVariable String crypto);
	@GetMapping("/crypto-wallets/emails")
	public List<String> getAllCryptoWalletEmails();
	@GetMapping("/crypto-wallet/id/{email}")
	public Long getAccountIdByEmail(@PathVariable String email);
	@PutMapping("/bank-account/account/{bankAccountId}/currency/{currency}")
	public ResponseEntity<String> addCurrencyToAccount(@PathVariable long bankAccountId, @PathVariable String currency);
}
