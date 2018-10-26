package smokefree;

import io.micronaut.context.annotation.Bean;
import io.micronaut.context.annotation.Factory;
import lombok.NoArgsConstructor;
import org.axonframework.commandhandling.gateway.CommandGateway;

import javax.inject.Singleton;

@Factory
@NoArgsConstructor
public class AxonFactory {
    @Bean
    @Singleton
    public CommandGateway commandGateway() {
        return null;
    }
}
