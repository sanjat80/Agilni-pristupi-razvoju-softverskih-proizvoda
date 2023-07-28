package bankAccount;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.UUID;
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
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.netflix.discovery.AbstractDiscoveryClientOptionalArgs;

import bankAccount.models.BankAccount;
import bankAccount.models.CurrenciesAmount;

@RestController
public class BankAccountController {

	@Autowired
	private BankAccountRepository repo;

	@Autowired
	private CurrenciesAmountRepository repoAcc;

	@Autowired
	private UsersServiceProxy proxy;
	
	@Autowired(required = true)
	private AbstractDiscoveryClientOptionalArgs<?> optionalArgs = null;

	@GetMapping("/bank-account/accounts")
	public List<BankAccount> getAllAccounts() {
		return repo.findAll();
	}
	//DODAVANJE RACUNA
	@PostMapping("/bank-account/account")
	public ResponseEntity<?> createAccount(@RequestBody BankAccount account, @RequestHeader("Authorization") String authorizationHeader) {
		String role = proxy.extractRole(authorizationHeader);
		//System.out.println(role);
		if(role.equals("ADMIN")) {
		if (repo.existsById(account.getAccountID())) {
			String errorMessage = "Account with passed id already exists.";
			return ResponseEntity.status(HttpStatus.CONFLICT).body(errorMessage);
		} else {
			if (!repo.existsByEmail(account.getEmail())) {
				Boolean emailUser = proxy.getUser(account.getEmail()); 
				if (emailUser.equals(false)) {
					String errorMessage = "User with email doesn't exist.";
					return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorMessage);
				} else {
					String roleUser = proxy.getUsersRole(account.getEmail()); 
					if (!roleUser.equals("USER")) {
						String errorMessage = "User doesn't have role 'USER'.";
						return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorMessage);
					} else {
						List<CurrenciesAmount> savedCurrencies = new ArrayList<>();
						BankAccount createdAccount = repo.save(account);
						for (CurrenciesAmount currency : account.getCurrencies()) {
							currency.setBankAccount(createdAccount);
							CurrenciesAmount savedCurrency = repoAcc.save(currency);
							savedCurrencies.add(savedCurrency);
						}
						createdAccount.setCurrencies(savedCurrencies);
						return new ResponseEntity<>(createdAccount, HttpStatus.CREATED);
					}
				}
			} else {
				String errorMessage = "This user already have an bank account.";
				return ResponseEntity.status(HttpStatus.CONFLICT).body(errorMessage);
			}
		}
		} else {
			String msg = "Only user who has role 'ADMIN' can add new Bank Account.";
			return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(msg);
		}
	}
	//AZURIRANJE RACUNA
	@PutMapping("/bank-account/account/{accountID}")
	public ResponseEntity<?> updateAccount(@PathVariable long accountID, @RequestBody BankAccount updatedAccount, @RequestHeader("Authorization") String authorizationHeader) {
		String role = proxy.extractRole(authorizationHeader);
		if(role.equals("ADMIN")) {
		BankAccount existingAccount = repo.findById(accountID).orElse(null);
		if (existingAccount == null) {
			String errorMessage = "The account with passed id doesn't exists.";
			return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorMessage);
		} else {
			if (existingAccount.getEmail().equals(updatedAccount.getEmail())) {
				for (CurrenciesAmount currency : updatedAccount.getCurrencies()) {
					currency.setBankAccount(existingAccount);
					repoAcc.save(currency);
				}
				BankAccount updatedBankAccount = repo.save(updatedAccount);
				return ResponseEntity.ok(updatedBankAccount);
			} else {
				String errorMessage = "You can't update account email.";
				return ResponseEntity.status(HttpStatus.CONFLICT).body(errorMessage);

			}
		}
		} else {
			String msg = "Only user who has role 'ADMIN' can add new Bank Account.";
			return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(msg);
		}
	}
	@GetMapping("/bank-account/account/{accountID}")
	public BankAccount getBankAccountById(@PathVariable long accountId)
	{
		BankAccount account = repo.getById(accountId);
		return account;	
	}
	@GetMapping("/bank-account/email/{email}")
	public BankAccount getAccountByEmail(@PathVariable String email)
	{
		BankAccount account = repo.findByEmail(email);
		return account;	
	}
	@GetMapping("/bank-account/id/{email}")
	public Long getAccountIdByEmail(@PathVariable String email)
	{
		BankAccount account = repo.findByEmail(email);
		Long id = account.getAccountID();
		return id;	
	}
	@GetMapping("/bank-account/account/email/{email}")
	public ResponseEntity<String> getBankAccountByEmail(@PathVariable String email)
	{
		BankAccount account = repo.findByEmail(email);
		if(account == null)
		{
			String message = "Bankovni racun za dati email nije pronadjen.";
			return ResponseEntity.status(HttpStatus.NOT_FOUND).body(message);
		}
		String accountEmail = account.getEmail();
		return ResponseEntity.ok(accountEmail);	
	}
	@DeleteMapping("/bank-account/account/{email}")
	public void deleteAccount(@PathVariable String email) {
		BankAccount bankAccount = repo.findByEmail(email);
	    if (bankAccount != null) {
	        repo.delete(bankAccount);
	    } 
	}
	//BRISANJE RACUNA
	@DeleteMapping("/bank-account/{accountID}")
	public ResponseEntity<String> deleteBankAccount(@PathVariable Long accountID, @RequestHeader("Authorization") String authorizationHeader) {
		String role = proxy.extractRole(authorizationHeader);
		if(role.equals("ADMIN")) {
		 Optional<BankAccount> optionalBankAccount = repo.findById(accountID);
		    if (optionalBankAccount.isPresent()) {
		        BankAccount bankAccount = optionalBankAccount.get();
		        repo.delete(bankAccount);
		        return ResponseEntity.ok("Bankovni racun izbrisan.");
		    } else {
		    	String msg = "Bankovni racun sa datim id-em nije pronadjen.";
		        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(msg);
		    }
		} else {
			String msg = "Samo korisnik sa ulogom 'ADMIN' ima mogucnost brisanja racuna.";
			return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(msg);
		}
	}
	/*@GetMapping("/bank-account/account/{email}/{currency}")
	public double getCurrentAmount(@PathVariable String email, @PathVariable String currency) {
	    BankAccount bankAccount = repo.findByEmail(email);
	    double amount = 0.0;
	    
	    if (bankAccount != null) {
	        List<CurrenciesAmount> currencies = bankAccount.getCurrencies();
	        
	        for (CurrenciesAmount currencyAmount : currencies) {
	            if (currencyAmount.getCurrency().equals(currency)) {
	                amount = currencyAmount.getAmount();
	            }
	        }
	    }
	   
	    return amount;
	}*/
	@PutMapping("/bank-account/account/{bankAccountId}/{currency}")
	public ResponseEntity<String> updateAount(@PathVariable Long bankAccountId, @PathVariable String currency, @RequestParam Double newAmount) {
	    BankAccount bankAccount = repo.findById(bankAccountId).orElse(null);
	    
	    if (bankAccount != null) {
	        List<CurrenciesAmount> currencies = bankAccount.getCurrencies();
	        
	        for (CurrenciesAmount currencyAmount : currencies) {
	            if (currencyAmount.getCurrency().equals(currency)) {
	                currencyAmount.setAmount(newAmount);
	                repo.save(bankAccount);
	                return ResponseEntity.ok("Iznos je uspješno ažuriran");
	            }
	        }
	    }
	    
	    return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Bankovni račun nije pronađen");
	}
	@PutMapping("/bank-account/amount/{email}/{currency}")
	public ResponseEntity<String> updateAmount(@PathVariable String email, @PathVariable String currency, @RequestParam Double newAmount) {
	    BankAccount bankAccount = repo.findByEmail(email);
	    
	    if (bankAccount != null) {
	        List<CurrenciesAmount> currencies = bankAccount.getCurrencies();
	        
	        for (CurrenciesAmount currencyAmount : currencies) {
	            if (currencyAmount.getCurrency().equals(currency)) {
	                currencyAmount.setAmount(newAmount);
	                repo.save(bankAccount);
	                return ResponseEntity.ok("Iznos je uspješno ažuriran");
	            }
	        }
	       
	    }
	    
	    return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Bankovni račun nije pronađen");
	}
	@GetMapping("/bank-account/account/{email}/{currency}")
	public Double getCurrentAmountForCurrency(@PathVariable String email, @PathVariable String currency)
	{
		try {
		BankAccount account = repo.findByEmail(email);
		if(account != null)
		{
			 List<CurrenciesAmount> currencies = account.getCurrencies();
	            for (CurrenciesAmount currenciesAmount : currencies) {
	                if (currenciesAmount.getCurrency().equals(currency)) {
	                    return currenciesAmount.getAmount();
	                }
	            }
	            return null;
	        } else {
	            return null;
	        }
	    } catch (Exception e) {
	        return null;
	    }
	}
	
	@GetMapping("/bank-accounts/emails")
	public List<String> getAllBankAccountEmails() {
	    List<BankAccount> bankAccounts = repo.findAll();
	    List<String> emails = bankAccounts.stream()
	            .map(BankAccount::getEmail)
	            .collect(Collectors.toList());
	    return emails;
	}
	@PutMapping("/bank-account/account/{bankAccountId}/currency/{currency}")
	public ResponseEntity<String> addCurrencyToAccount(@PathVariable long bankAccountId, @PathVariable String currency) {
	    BankAccount bankAccount = repo.findById(bankAccountId).orElse(null);
	    
	    if (bankAccount != null) {
	        List<CurrenciesAmount> currencies = bankAccount.getCurrencies();
	        
	        for (CurrenciesAmount currencyAmount : currencies) {
	            if (currencyAmount.getCurrency().equals(currency)) {
	                return ResponseEntity.status(HttpStatus.CONFLICT).body("Valuta već postoji u novčaniku");
	            }
	        }
	        
	        CurrenciesAmount newCurrencyAmount = new CurrenciesAmount();
	        newCurrencyAmount.setId(generateRandomId());
	        newCurrencyAmount.setCurrency(currency);
	        newCurrencyAmount.setAmount(0.0);
	        newCurrencyAmount.setBankAccount(bankAccount);
	        
	        currencies.add(newCurrencyAmount);
	        
	        repo.save(bankAccount);
	        
	        return ResponseEntity.ok("Nova valuta je uspješno dodana u novčanik");
	    }
	    
	    return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Bankovni račun nije pronađen");
	}
	

	public Long generateRandomId() {
	    Random random = new Random();
	    return random.nextLong();
	}



}