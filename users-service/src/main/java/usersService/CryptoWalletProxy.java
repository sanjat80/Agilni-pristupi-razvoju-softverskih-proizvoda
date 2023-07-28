package usersService;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "crypto-wallet")
public interface CryptoWalletProxy {
	@DeleteMapping("/crypto-wallet/wallet/{email}")
	public void deleteUserWallet(@PathVariable String email);
}
