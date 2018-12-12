package smokefree;

import io.micronaut.core.util.StringUtils;
import io.micronaut.security.authentication.Authentication;
import io.micronaut.security.authentication.AuthenticationException;

import javax.annotation.Nullable;
import java.util.concurrent.ConcurrentHashMap;

@SuppressWarnings("WeakerAccess")
public class SecurityContext extends ConcurrentHashMap<String, Object> {
    SecurityContext(Authentication authentication) {
        if (authentication != null) {
            put("authentication", authentication);
        }
    }

    public Authentication authentication() {
        return (Authentication) get("authentication");
    }

    /**
     * In JWT / Cognito, the 'sub' ID is considered the unique 'username'.
     */
    public @Nullable
    String userId() {
        final Authentication authentication = authentication();
        if (authentication == null) {
            return null;
        }
        return authentication.getName();
    }

    /**
     * In JWT / Cognito, the 'sub' ID is considered the unique 'username'.
     */
    public String requireUserId() {
        final Authentication authentication = authentication();
        if (authentication == null) {
            throw new AuthenticationException("Not logged in");
        }
        return authentication.getName();
    }

    public String requireUserName() {
        final Authentication authentication = authentication();
        if (authentication == null) {
            throw new AuthenticationException("Not logged in");
        }
        String userName = (String) authentication.getAttributes().get("cognito:username");
        if (StringUtils.isEmpty(userName)) {
            throw new AuthenticationException("No username");
        }
        return userName;
    }


}
