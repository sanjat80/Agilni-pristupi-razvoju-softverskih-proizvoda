package bankAccount;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import bankAccount.models.BankAccount;

public interface BankAccountRepository extends JpaRepository<BankAccount,Long>{
	boolean existsByEmail(String email);
	BankAccount findByEmail(String email);
	List<BankAccount> findAll();
	Optional<BankAccount> findById(Long accountID);
}
