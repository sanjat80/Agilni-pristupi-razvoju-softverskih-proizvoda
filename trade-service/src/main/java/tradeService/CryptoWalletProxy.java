package tradeService;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient("crypto-wallet")
public interface CryptoWalletProxy {
	@PutMapping("/crypto-wallet/amount/{email}/{currency}")
	public ResponseEntity<String> updateCryptoWallet(@PathVariable String email, @PathVariable String currency, @RequestParam double amount);
	@GetMapping("/crypto-wallet/wallet/email/{email}")
	public CryptoWalletDto getCryptoWalletByEmail(@PathVariable String email);
	@GetMapping("/crypto-wallet/wallet/{email}/{crypto}")
	public double getCurrentAmount(@PathVariable String email, @PathVariable String crypto);
}
