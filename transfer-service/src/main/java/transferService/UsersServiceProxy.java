package transferService;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;

@FeignClient("users-service")
public interface UsersServiceProxy {
	@GetMapping("/users-service/logged-user")
	public String getCurrentRole(@RequestHeader("Authorization") String authorizationHeader);
	@GetMapping("/users-service/email-logged-user")
	public String getUserEmail(@RequestHeader("Authorization") String authorizationHeader);
	@GetMapping("/users-service/id-logged-user")
	public Long getUserId(@RequestHeader("Authorization") String authorizationHeader);
}
