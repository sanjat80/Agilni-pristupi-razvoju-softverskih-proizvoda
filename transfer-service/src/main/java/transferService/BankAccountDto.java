package transferService;

import java.util.List;


public class BankAccountDto {
	private long accountId;
    private List<CurrenciesAmountDto> currencies;
    private String email;

    public BankAccountDto() {
    }

    public long getAccountId() {
        return accountId;
    }

    public void setAccountId(long accountId) {
        this.accountId = accountId;
    }

    public List<CurrenciesAmountDto> getCurrencies() {
        return currencies;
    }

    public void setCurrencies(List<CurrenciesAmountDto> currencies) {
        this.currencies = currencies;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }
}
