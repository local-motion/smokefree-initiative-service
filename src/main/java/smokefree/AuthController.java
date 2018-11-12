package smokefree;

import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Get;
import io.micronaut.security.annotation.Secured;

import java.security.Principal;

@Secured("isAuthenticated()") 
@Controller("/auth")
public class AuthController {

    @Get("/signin")
    String signin(Principal principal) {
        return principal.getName();
    }
}