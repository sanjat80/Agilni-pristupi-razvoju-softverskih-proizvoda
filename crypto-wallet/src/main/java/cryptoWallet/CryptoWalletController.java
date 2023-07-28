package cryptoWallet;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import cryptoWallet.models.CryptoWallet;
import cryptoWallet.models.CryptosAmount;
import io.github.resilience4j.retry.annotation.Retry;

@RestController
public class CryptoWalletController {
	
	@Autowired
	private CryptoWalletRepository repo;
	
	@Autowired
	private CryptosAmountRepository repoWall;
	
	@Autowired
	private UsersServiceProxy proxy;
	
	@Autowired
	private BankAccountProxy proxyBank;
	
	@GetMapping("/crypto-wallet/wallets")
	public List<CryptoWallet> getAllWallets() {
		return repo.findAll();
	}
	
	/*@PostMapping("/crypto-wallet/wallet")
	public ResponseEntity<?> createWallet(@RequestBody CryptoWallet wallet) {
		if (repo.existsById(wallet.getWalletID())) {
			String errorMessage = "Wallet with passed id already exists.";
			return ResponseEntity.status(HttpStatus.CONFLICT).body(errorMessage);
		} else {
			if (!repo.existsByEmail(wallet.getEmail())) {
				Boolean emailUser = proxy.getUser(wallet.getEmail()); // provera da li u bazi za korisnike postoji
																		// korisnik za koji se kreira racun tj da li
																		// postoji email kor koji ce biti pridodat
																		// bankovnom r
				if (emailUser.equals(false)) {
					String errorMessage = "User with email doesn't exist.";
					return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorMessage);
				} else {
					String roleUser = proxy.getUsersRole(wallet.getEmail()); // ako postoji korinik proverava se da li
																				// je korisnik USER
					if (!roleUser.equals("USER")) {
						String errorMessage = "User doesn't have role 'USER'.";
						return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorMessage);
					} else {
						List<CryptosAmount> savedCryptos = new ArrayList<>();
						CryptoWallet createdWallet = repo.save(wallet);
						for (CryptosAmount crypto : wallet.getCryptos()) {
							crypto.setCryptoWallet(createdWallet);
							CryptosAmount savedCrypto = repoWall.save(crypto);
							savedCryptos.add(savedCrypto);
						}
						createdWallet.setCurrencies(savedCryptos);
						return new ResponseEntity<>(createdWallet, HttpStatus.CREATED);
					}
				}
			} else {
				String errorMessage = "This user already have an bank account.";
				return ResponseEntity.status(HttpStatus.CONFLICT).body(errorMessage);
			}
		}
	}*/
	@PostMapping("/crypto-wallet/wallet")
	@Retry(name = "myRetry", fallbackMethod = "fallbackCreateWallet")
	public ResponseEntity<?> createWallet(@RequestBody CryptoWallet wallet) {
	    if (repo.existsById(wallet.getWalletID())) {
	        String errorMessage = "Wallet with the passed ID already exists.";
	        return ResponseEntity.status(HttpStatus.CONFLICT).body(errorMessage);
	    } else {
	        if (repo.existsByEmail(wallet.getEmail())) {
	            String errorMessage = "This user already has a crypto wallet."; //ako zelim da pridruzim jos jedan wallet postojecem korisniku
	            																//to je uslov da korisnik/bank account ima samo jedan wallet
	            return ResponseEntity.status(HttpStatus.CONFLICT).body(errorMessage); //ako ne postoji bankovni racun za taj email koji zelimo da stavimo za email ovd
	        } else {
	            if (proxyBank.getAccountByEmail(wallet.getEmail())==null) {
	                String errorMessage = "Crypto wallet for passed email could not be found.";
	                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorMessage);
	            } else {
	                String roleUser = proxy.getUsersRole(wallet.getEmail());
	                if (!roleUser.equals("USER")) {
	                    String errorMessage = "User doesn't have the 'USER' role.";
	                    return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorMessage);
	                } else {
	                	String bankAccMail= proxyBank.getAccountByEmail(wallet.getEmail());
	                	if(bankAccMail.equals(wallet.getEmail())) {
	                    List<CryptosAmount> savedCryptos = new ArrayList<>();
	                    CryptoWallet createdWallet = repo.save(wallet);
	                    for (CryptosAmount crypto : wallet.getCryptos()) {
	                        crypto.setCryptoWallet(createdWallet);
	                        CryptosAmount savedCrypto = repoWall.save(crypto);
	                        savedCryptos.add(savedCrypto);
	                    }
	                    createdWallet.setCryptos(savedCryptos);
	                    return new ResponseEntity<>(createdWallet, HttpStatus.CREATED);
	                } else {
	                	String msg = "Bank account mail and crypto wallet email do not match.";
	                	return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(msg);
	                }
	            }
	        }
	    }
	   }
	}

	
	@PutMapping("/crypto-wallet/wallet/{walletID}")
	public ResponseEntity<?> updateWallet(@PathVariable long walletID, @RequestBody CryptoWallet updatedWallet) {
		CryptoWallet existingWallet = repo.findById(walletID).orElse(null);
		if (existingWallet == null) {
			String errorMessage = "The wallet with passed id doesn't exists.";
			return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorMessage);
		} else {
			if (existingWallet.getEmail().equals(updatedWallet.getEmail())) {
				for (CryptosAmount crypto : updatedWallet.getCryptos()) {
					crypto.setCryptoWallet(existingWallet);
					repoWall.save(crypto);
				}
				CryptoWallet updatedCryptoWallet = repo.save(updatedWallet);
				return ResponseEntity.ok(updatedCryptoWallet);
			} else {
				String errorMessage = "You can't update wallet email.";
				return ResponseEntity.status(HttpStatus.CONFLICT).body(errorMessage);

			}
		}
	}
	
	/*@GetMapping("/crypto-wallet/wallet/{walletID}")
	public CryptoWallet getCryptoWalletById(@PathVariable long walletID)
	{
		CryptoWallet wallet = repo.getById(walletID);
		return wallet;	
	}*/
	@GetMapping("/crypto-wallet/id/{email}")
	public Long getAccountIdByEmail(@PathVariable String email)
	{
		CryptoWallet account = repo.findByEmail(email);
		Long id = account.getWalletID();
		return id;	
	}
	@DeleteMapping("/crypto-wallet/wallet/{email}")
	public void deleteWallet(@PathVariable String email) {
		CryptoWallet cryptoWallet = repo.findByEmail(email);
	    if (cryptoWallet != null) {
	        repo.delete(cryptoWallet);
	    } 
	}
	@GetMapping("/crypto-wallet/wallet/email/{email}")
	public CryptoWallet getCryptoWalletByEmail(@PathVariable String email)
	{
		CryptoWallet wallet = repo.findByEmail(email);
		return wallet;	
	}
	/*@GetMapping("/crypto-wallet/wallet/{email}/{crypto}")
	public ResponseEntity<Double> getCurrentAmount(@PathVariable String email, @PathVariable String crypto) {
	    CryptoWallet cryptoWallet = repo.findByEmail(email);
	    double amount = 0.0;
	    
	    if (cryptoWallet != null) {
	        List<CryptosAmount> cryptos = cryptoWallet.getCryptos();
	        
	        for (CryptosAmount cryptoAmount : cryptos) {
	            if (cryptoAmount.getCrypto().equals(crypto)) {
	                amount = cryptoAmount.getAmount();
	            }
	        }
	    }
	   
	    return ResponseEntity.status(HttpStatus.OK).body(amount);
	}*/
	@GetMapping("/crypto-wallet/wallet/{email}/{crypto}")
	public Double getCurrentAmount(@PathVariable String email, @PathVariable String crypto) {
	    CryptoWallet cryptoWallet = repo.findByEmail(email);
	    double amount = 0.0;

	    if (cryptoWallet != null) {
	        List<CryptosAmount> cryptos = cryptoWallet.getCryptos();

	        if (cryptos.isEmpty()) {
	            return null; // Novčanik je prazan
	        }

	        for (CryptosAmount cryptoAmount : cryptos) {
	            if (cryptoAmount.getCrypto().equals(crypto)) {
	                amount = cryptoAmount.getAmount();
	            }
	        }
	    } else {
	        return null; // Novčanik nije pronađen
	    }

	    return amount;
	}
	
	@PutMapping("/crypto-wallet/wallet/{cryptoWalletId}/{crypto}")
	public ResponseEntity<String> updateAmount(@PathVariable Long cryptoWalletId, @PathVariable String crypto, @RequestParam double newAmount) {
	    CryptoWallet cryptoWallet = repo.findById(cryptoWalletId).orElse(null);
	    
	    if (cryptoWallet != null) {
	        List<CryptosAmount> cryptos = cryptoWallet.getCryptos();
	        
	        for (CryptosAmount cryptoAmount : cryptos) {
	            if (cryptoAmount.getCrypto().equals(crypto)) {
	                cryptoAmount.setAmount(newAmount);
	                // Spremi izmijenjene podatke u bazu ili repozitorij
	                repo.save(cryptoWallet);
	                return ResponseEntity.ok("Iznos je uspješno ažuriran");
	            }
	        }
	        
	        // Ako nije pronađen odgovarajući valutni iznos, možete dodati logiku za kreiranje novog valutnog iznosa
	        // ili vratiti odgovarajući HTTP status kod
	        return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Valutni iznos nije pronađen");
	    }else {	    
	    	return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Kripto novcanik nije pronađen");
	    }
	}
	
	@PutMapping("/crypto-wallet/amount/{email}/{currency}")
	public ResponseEntity<String> updateCryptoWallet(@PathVariable String email, @PathVariable String currency, @RequestParam Double amount) {
		CryptoWallet wallet = repo.findByEmail(email);
	    
	    if (wallet != null) {
	        List<CryptosAmount> currencies = wallet.getCryptos();
	        
	        for (CryptosAmount currencyAmount : currencies) {
	            if (currencyAmount.getCrypto().equals(currency)) {
	                currencyAmount.setAmount(amount);
	                repo.save(wallet);
	                return ResponseEntity.ok("Iznos je uspješno ažuriran");
	            }
	        }
	        return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Valutni iznos nije pronađen");
	    }
	    
	    return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Bankovni račun nije pronađen");
	}
	
	@GetMapping("/crypto-wallets/emails")
	public List<String> getAllCryptoWalletEmails() {
	    List<CryptoWallet> cryptoWallets = repo.findAll();
	    List<String> emails = cryptoWallets.stream()
	            .map(CryptoWallet::getEmail)
	            .collect(Collectors.toList());
	    return emails;
	}
	@PutMapping("/bank-account/account/{bankAccountId}/currency/{currency}")
	public ResponseEntity<String> addCurrencyToAccount(@PathVariable long bankAccountId, @PathVariable String currency) {
	   CryptoWallet cryptoWallet = repo.findById(bankAccountId).orElse(null);
	    
	    if (cryptoWallet != null) {
	        List<CryptosAmount> cryptos = cryptoWallet.getCryptos();
	        
	        for (CryptosAmount currencyAmount : cryptos) {
	            if (currencyAmount.getCrypto().equals(currency)) {
	                return ResponseEntity.status(HttpStatus.CONFLICT).body("Valuta već postoji u novčaniku");
	            }
	        }
	        
	        CryptosAmount newCryptoAmount = new CryptosAmount();
	        newCryptoAmount.setId(generateRandomId());
	        newCryptoAmount.setCrypto(currency);
	        newCryptoAmount.setAmount(0.0);
	        newCryptoAmount.setCryptoWallet(cryptoWallet);
	        
	        cryptos.add(newCryptoAmount);
	        
	        repo.save(cryptoWallet);
	        
	        return ResponseEntity.ok("Nova valuta je uspješno dodana u novčanik");
	    }
	    
	    return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Kripto novcanik nije pronađen");
	}
	

	public Long generateRandomId() {
	    Random random = new Random();
	    return random.nextLong();
	}
	
	public ResponseEntity<?> fallbackCreateWallet(@RequestBody CryptoWallet wallet) {
	    String errorMessage = "Došlo je do greške prilikom kreiranja novčanika.";
	    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorMessage);
	}

	
}
