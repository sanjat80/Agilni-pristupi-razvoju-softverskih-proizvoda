package tradeService;

import java.util.List;

public class CryptoWalletDto {
	private long walletID;
	private List<CryptosAmountDto> cryptos;
	private String email;
	
	public CryptoWalletDto() {
		// prazan konstruktor
	}

	public long getWalletID() {
		return walletID;
	}

	public void setWalletID(long walletID) {
		this.walletID = walletID;
	}

	public List<CryptosAmountDto> getCryptos() {
		return cryptos;
	}

	public void setCryptos(List<CryptosAmountDto> cryptos) {
		this.cryptos = cryptos;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}
}
