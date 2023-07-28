package cryptoCurrency;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestParam;


@FeignClient(name="crypto-wallet")
public interface CryptoWalletProxy {
	@GetMapping("/crypto-wallet/wallet/{email}/{crypto}")
	 double getCurrentAmount(@PathVariable("email") String email, @PathVariable("crypto") String crypto);
	@PutMapping("/crypto-wallet/wallet/{cryptoWalletId}/{crypto}")
	 void updateAmount(@PathVariable("cryptoWalletId") Long cryptoWalletId, @PathVariable("crypto") String crypto, @RequestParam double newAmount);
	@GetMapping("/crypto-wallet/wallet/email/{email}")
	public CryptoWalletDto getCryptoWalletByEmail(@PathVariable String email);
	 @GetMapping("/crypto-wallet/id/{email}")
	public Long getAccountIdByEmail(@PathVariable String email);
}
