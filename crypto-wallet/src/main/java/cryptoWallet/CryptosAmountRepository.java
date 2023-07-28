package cryptoWallet;

import org.springframework.data.jpa.repository.JpaRepository;

import cryptoWallet.models.CryptosAmount;

public interface CryptosAmountRepository extends JpaRepository<CryptosAmount, Long> {

}
