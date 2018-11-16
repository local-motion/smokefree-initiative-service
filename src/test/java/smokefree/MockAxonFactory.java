package smokefree;

import io.micronaut.context.annotation.Bean;
import io.micronaut.context.annotation.Factory;
import io.micronaut.context.annotation.Replaces;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.axonframework.commandhandling.gateway.CommandGateway;
import org.axonframework.config.Configuration;
import org.axonframework.config.DefaultConfigurer;
import org.axonframework.eventsourcing.eventstore.inmemory.InMemoryEventStorageEngine;

@Slf4j
@Factory
@Replaces(factory = AxonFactory.class)
@NoArgsConstructor
public class MockAxonFactory {
    @Bean
    public Configuration configuration() {
        Configuration config = DefaultConfigurer
                .defaultConfiguration()
                .configureEmbeddedEventStore(c -> new InMemoryEventStorageEngine())
                .buildConfiguration();
        config.start();
        return config;
    }

    @Bean
    public CommandGateway gateway(Configuration configuration) {
        return configuration.commandGateway();
    }
}
