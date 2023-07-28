package cryptoCurrency;

import java.util.List;


public class CryptoWalletDto {
	
	private long walletId;
    private List<CryptosAmountDto> cryptos;
    private String email;

    public CryptoWalletDto() {
    }

    public long getWalletId() {
        return walletId;
    }

    public void setWalletId(long walletId) {
        this.walletId = walletId;
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
