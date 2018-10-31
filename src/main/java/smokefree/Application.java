package smokefree;

import io.micronaut.context.annotation.Bean;
import io.micronaut.runtime.Micronaut;
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