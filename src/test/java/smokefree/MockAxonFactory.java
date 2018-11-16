package smokefree;

import io.micronaut.context.annotation.Bean;
import io.micronaut.context.annotation.Factory;
import io.micronaut.context.annotation.Replaces;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.axonframework.commandhandling.SimpleCommandBus;
import org.axonframework.commandhandling.gateway.CommandGateway;
import org.axonframework.config.Configuration;
import org.axonframework.config.DefaultConfigurer;
import org.axonframework.eventhandling.SimpleEventBus;
import org.axonframework.eventsourcing.eventstore.inmemory.InMemoryEventStorageEngine;
import org.axonframework.modelling.command.*;
import org.axonframework.queryhandling.SimpleQueryBus;

import javax.inject.Singleton;

@Slf4j
@Factory
@Replaces(factory = AxonFactory.class)
@NoArgsConstructor
public class MockAxonFactory {
    @Singleton
    public Configuration configuration() {
        Configuration config = DefaultConfigurer
                .defaultConfiguration()
                .configureEmbeddedEventStore(c -> new InMemoryEventStorageEngine())
                .configureCommandBus(c -> SimpleCommandBus.builder().build())
                .configureEventBus(c -> SimpleEventBus.builder().build())
                .configureQueryBus(c -> SimpleQueryBus.builder().build())

                .buildConfiguration();
        config.start();
        return config;
    }

    @Bean
    public CommandGateway gateway(Configuration configuration) {
        return configuration.commandGateway();
    }
}
