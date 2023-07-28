package cryptoWallet;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.web.bind.annotation.PathVariable;

import cryptoWallet.models.CryptoWallet;

public interface CryptoWalletRepository extends JpaRepository<CryptoWallet, Long>{
	boolean existsByEmail(String email);
	CryptoWallet findByEmail(String email);
	CryptoWallet getById(Long walletID);
	List<CryptoWallet> findAll();
}
