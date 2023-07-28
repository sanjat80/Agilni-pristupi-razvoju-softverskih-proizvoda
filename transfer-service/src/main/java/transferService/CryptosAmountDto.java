package transferService;

public class CryptosAmountDto {
	 private Long id;
	    private String crypto;
	    private Double amount;
	    private Long walletID;

	    public CryptosAmountDto() {
	        // prazan konstruktor
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

	    public Long getWalletID() {
	        return walletID;
	    }

	    public void setWalletID(Long walletID) {
	        this.walletID = walletID;
	    }
}
