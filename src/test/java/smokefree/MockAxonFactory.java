package smokefree;

import io.micronaut.context.annotation.Bean;
import io.micronaut.context.annotation.Factory;
import io.micronaut.context.annotation.Replaces;
import io.micronaut.context.annotation.Requires;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.axonframework.commandhandling.SimpleCommandBus;
import org.axonframework.commandhandling.gateway.CommandGateway;
import org.axonframework.common.jdbc.DataSourceConnectionProvider;
import org.axonframework.common.transaction.NoTransactionManager;
import org.axonframework.config.Configuration;
import org.axonframework.config.DefaultConfigurer;
import org.axonframework.eventhandling.EventBus;
import org.axonframework.eventhandling.SimpleEventBus;
import org.axonframework.eventsourcing.eventstore.EmbeddedEventStore;
import org.axonframework.eventsourcing.eventstore.inmemory.InMemoryEventStorageEngine;
import org.axonframework.eventsourcing.eventstore.jdbc.JdbcEventStorageEngine;
import org.axonframework.eventsourcing.eventstore.jdbc.MySqlEventTableFactory;
import org.axonframework.modelling.command.*;
import org.axonframework.queryhandling.SimpleQueryBus;
import org.axonframework.serialization.Serializer;
import org.axonframework.serialization.upcasting.event.NoOpEventUpcaster;

import javax.inject.Named;
import javax.inject.Singleton;
import javax.sql.DataSource;

@Slf4j
@Factory
@Requires(env = "test")
@Replaces(factory = AxonFactory.class)
@NoArgsConstructor
public class MockAxonFactory {
    @Singleton
    public Configuration configuration(EventBus eventBus) {
        Configuration config = DefaultConfigurer
                .defaultConfiguration()
                .configureEmbeddedEventStore(c -> new InMemoryEventStorageEngine())
                .configureCommandBus(c -> SimpleCommandBus.builder().build())
                .configureEventBus(c -> eventBus )
                .configureQueryBus(c -> SimpleQueryBus.builder().build())

                .buildConfiguration();
        config.start();
        return config;
    }

    @Singleton
    public EventBus eventBus( DataSource dataSource, Serializer serializer) {

        JdbcEventStorageEngine engine = JdbcEventStorageEngine.builder()
                .connectionProvider(new DataSourceConnectionProvider(dataSource))
                .transactionManager(NoTransactionManager.instance())
                .eventSerializer(serializer)
                .snapshotSerializer(serializer)
                .upcasterChain(NoOpEventUpcaster.INSTANCE)
                .build();
        engine.createSchema(new MySqlEventTableFactory());

        return EmbeddedEventStore.builder().storageEngine(engine).build();
    }

    @Bean
    public CommandGateway gateway(Configuration configuration) {
        return configuration.commandGateway();
    }
}
