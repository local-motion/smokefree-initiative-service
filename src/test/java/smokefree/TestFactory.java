package smokefree;

import io.micronaut.context.annotation.Bean;
import io.micronaut.context.annotation.Factory;
import io.micronaut.security.authentication.AuthenticationUserDetailsAdapter;
import io.micronaut.security.authentication.UserDetails;
import io.micronaut.security.token.validator.TokenValidator;
import io.reactivex.Flowable;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import static com.google.common.collect.Sets.newHashSet;

@Slf4j
@Factory
@NoArgsConstructor
public class TestFactory {
    /**
     * Allows for ignoring security during unit tests.
     */
    @Bean
    public TokenValidator disableAuthentication() {
        return token -> {
            UserDetails userDetails = new UserDetails("unit-test-1", newHashSet());
            AuthenticationUserDetailsAdapter authentication = new AuthenticationUserDetailsAdapter(userDetails);
            return Flowable.just(authentication);
        };
    }
}
