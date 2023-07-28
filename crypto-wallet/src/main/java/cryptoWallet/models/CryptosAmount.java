package cryptoWallet.models;

import com.fasterxml.jackson.annotation.JsonIgnore;

import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;

@Entity
public class CryptosAmount {
	@Id
    private Long id;
	
	private String crypto;
	
    private Double amount;
  
    @ManyToOne(fetch = FetchType.LAZY)
    @JsonIgnore
    @JoinColumn(name = "walletID")
    private CryptoWallet cryptoWallet;

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getCrypto() {
		return crypto;
	}

	public void setCrypto(String crypto) {
		this.crypto = crypto;
	}

	public Double getAmount() {
		return amount;
	}

	public void setAmount(Double amount) {
		this.amount = amount;
	}

	public CryptoWallet getCryptoWallet() {
		return cryptoWallet;
	}

	public void setCryptoWallet(CryptoWallet cryptoWallet) {
		this.cryptoWallet = cryptoWallet;
	}
}
