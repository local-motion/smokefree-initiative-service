package smokefree;

import io.micronaut.context.annotation.Bean;
import io.micronaut.runtime.Micronaut;
import io.micronaut.security.token.jwt.signature.SignatureConfiguration;
import io.micronaut.security.token.jwt.validator.JwtTokenValidator;
import io.micronaut.security.token.validator.TokenValidator;
import smokefree.projection.InitiativeProjection;

public class Application {
    @Bean
    public InitiativeProjection initiativeProjection() {
        return new InitiativeProjection();
    }

    public static void main(String[] args) {
        Micronaut.run(Application.class);
    }
}