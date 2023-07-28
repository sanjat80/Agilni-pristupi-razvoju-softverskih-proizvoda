package bankAccount.models;

import java.util.List;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;

@Entity
public class BankAccount {
	
	@Id
	private long accountID;
		
	@OneToMany(mappedBy = "bankAccount", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    private List<CurrenciesAmount> currencies;
	
	@Column(unique = true, nullable = false)
	private String email;
	
	public BankAccount() {
		
	}

	public long getAccountID() {
		return accountID;
	}

	public void setAccountID(long accountID) {
		this.accountID = accountID;
	}

	public List<CurrenciesAmount> getCurrencies() {
		return currencies;
	}

	public void setCurrencies(List<CurrenciesAmount> currencies) {
		this.currencies = currencies;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

}
