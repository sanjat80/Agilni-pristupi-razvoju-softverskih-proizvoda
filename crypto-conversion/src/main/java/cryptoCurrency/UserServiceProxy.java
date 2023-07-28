package cryptoCurrency;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(name="users-service")
public interface UserServiceProxy {
	@GetMapping("/users-service/logged-user")
	public String getCurrentUser(@RequestHeader("Authorization") String authorizationHeader);
	@GetMapping("/users-service/email-logged-user")
	public String getEmailOfCurrentUser(@RequestHeader("Authorization") String authorizationHeader);
	@GetMapping("/users-service/id-logged-user/{email}")
	public Long extractUsersId(@PathVariable String email);
	
}
