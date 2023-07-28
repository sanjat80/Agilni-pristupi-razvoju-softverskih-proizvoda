package cryptoCurrency;

public class CryptosAmountDto {
	private Long id;
    private String crypto;
    private Double amount;
    private Long cryptoWalletId;

    // Default konstruktor
    public CryptosAmountDto() {
    }

    public CryptosAmountDto(Long id, String crypto, Double amount, Long cryptoWalletId) {
        this.id = id;
        this.crypto = crypto;
        this.amount = amount;
        this.cryptoWalletId = cryptoWalletId;
    }

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

    public Long getCryptoWalletId() {
        return cryptoWalletId;
    }

}
