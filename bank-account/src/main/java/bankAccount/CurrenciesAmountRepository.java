package bankAccount;

import org.springframework.data.jpa.repository.JpaRepository;

import bankAccount.models.CurrenciesAmount;

public interface CurrenciesAmountRepository extends JpaRepository<CurrenciesAmount,Long>{
	
}
