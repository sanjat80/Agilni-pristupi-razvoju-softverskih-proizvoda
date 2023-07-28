package cryptoWallet.models;

import java.util.List;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;

@Entity
public class CryptoWallet {
	
	@Id
	private long walletID;
	
	@OneToMany(mappedBy = "cryptoWallet", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
	private List<CryptosAmount> cryptos;
	
	@Column(unique=true, nullable = false)
	private String email;
	
	public CryptoWallet()
	{
		
	}
	
	public long getWalletID() {
		return walletID;
	}

	public void setWalletID(long walletID){
		this.walletID = walletID;
	}

	public List<CryptosAmount> getCryptos() {
		return cryptos;
	}

	public void setCryptos(List<CryptosAmount> cryptos) {
		this.cryptos = cryptos;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}
	
}
