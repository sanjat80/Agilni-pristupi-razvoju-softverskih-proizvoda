package currencyConversion;

public class CurrenciesAmountDto {
    private Long id;
    private String currency;
    private Double amount;
    private Long bankAccountID;

    // Default konstruktor
    public CurrenciesAmountDto() {
    }

    public CurrenciesAmountDto(Long id, String currency, Double amount, Long bankAccountID) {
        this.id = id;
        this.currency = currency;
        this.amount = amount;
        this.bankAccountID = bankAccountID;
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

    public Long getBankAccountID() {
        return bankAccountID;
    }

}
