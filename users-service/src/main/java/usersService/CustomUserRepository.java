package usersService;

import org.springframework.data.jpa.repository.JpaRepository;

import usersService.model.CustomUser;

public interface CustomUserRepository extends JpaRepository<CustomUser, Long> {
	CustomUser findByEmail(String email);

	boolean existsByRole(String string);
	
	CustomUser findByRole(String role);

}
