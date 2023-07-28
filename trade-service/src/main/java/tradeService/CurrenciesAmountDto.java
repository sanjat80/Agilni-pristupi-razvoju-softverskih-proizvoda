package tradeService;

public class CurrenciesAmountDto {
	 private Long id;
	    private String currency;
	    private Double amount;
	    private Long bankAccountId;

	    // Default konstruktor
	    public CurrenciesAmountDto() {
	    }

	    public CurrenciesAmountDto(Long id, String currency, Double amount, Long bankAccountId) {
	        this.id = id;
	        this.currency = currency;
	        this.amount = amount;
	        this.bankAccountId = bankAccountId;
	    }

	    public Long getId() {
	        return id;
	    }

	    public void setId(Long id) {
	        this.id = id;
	    }

	    public String getCurrency() {
	        return currency;
	    }

	    public void setCurrency(String currency) {
	        this.currency = currency;
	    }

	    public Double getAmount() {
	        return amount;
	    }

	    public void setAmount(Double amount) {
	        this.amount = amount;
	    }

	    public Long getBankAccountId() {
	        return bankAccountId;
	    }
}
