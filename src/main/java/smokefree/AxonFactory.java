package smokefree;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.micronaut.context.annotation.Factory;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.axonframework.commandhandling.CommandBus;
import org.axonframework.commandhandling.SimpleCommandBus;
import org.axonframework.commandhandling.gateway.CommandGateway;
import org.axonframework.commandhandling.gateway.DefaultCommandGateway;
import org.axonframework.common.jdbc.DataSourceConnectionProvider;
import org.axonframework.common.transaction.NoTransactionManager;
import org.axonframework.config.Configuration;
import org.axonframework.config.Configurer;
import org.axonframework.config.DefaultConfigurer;
import org.axonframework.eventhandling.EventBus;
import org.axonframework.eventsourcing.eventstore.EmbeddedEventStore;
import org.axonframework.eventsourcing.eventstore.jdbc.JdbcEventStorageEngine;
import org.axonframework.eventsourcing.eventstore.jdbc.MySqlEventTableFactory;
import org.axonframework.messaging.interceptors.BeanValidationInterceptor;
import org.axonframework.queryhandling.*;
import org.axonframework.serialization.Serializer;
import org.axonframework.serialization.json.JacksonSerializer;
import org.axonframework.serialization.upcasting.event.NoOpEventUpcaster;
import smokefree.domain.Initiative;
import smokefree.domain.User;
import smokefree.projection.InitiativeProjection;
import smokefree.projection.ProfileProjection;

import javax.inject.Named;
import javax.inject.Singleton;
import javax.sql.DataSource;

import static com.google.common.collect.Lists.newArrayList;

@Slf4j
@Factory
@NoArgsConstructor
public class AxonFactory {
    @Singleton
    public Configuration configuration(EventBus eventBus,
                                       CommandBus commandBus,
                                       QueryBus queryBus,
                                       Serializer serializer,
                                       InitiativeProjection initiativeProjection,
                                       ProfileProjection profileProjection) {

        // TODO: How to avoid hard-coding aggregates, event- and query handlers?
        Configurer configurer = DefaultConfigurer.defaultConfiguration()
                .configureEventBus(c -> eventBus)
                .configureCommandBus(c -> commandBus)
                .configureQueryBus(c -> queryBus)
                .configureSerializer(c -> serializer)
                .configureAggregate(Initiative.class)
                .configureAggregate(User.class)
                .registerQueryHandler(c -> initiativeProjection)
                .registerQueryHandler(c -> profileProjection);

        configurer.eventProcessing()
                .registerEventHandler(c -> initiativeProjection)
                .registerEventHandler(c -> profileProjection);

        Configuration configuration = configurer.buildConfiguration();
        configuration.start();
        return configuration;
    }


    @Singleton
    public Serializer jsonSerializer(ObjectMapper objectMapper) {
        return JacksonSerializer.builder()
                .objectMapper(objectMapper)
                .build();
    }

    @Singleton
    public EventBus eventBus(@Named("axon") DataSource dataSource, Serializer serializer) {
//    public EventBus eventBus(DataSource dataSource, Serializer serializer) {
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

    @Singleton
    public CommandBus commandBus() {
        return SimpleCommandBus.builder().build();
    }

    @Singleton
    public QueryBus queryBus() {
        SimpleQueryUpdateEmitter queryUpdateEmitter = SimpleQueryUpdateEmitter.builder().build();
        return SimpleQueryBus.builder()
                .queryUpdateEmitter(queryUpdateEmitter)
                .build();
    }

    @Singleton
    public QueryGateway queryGateway(QueryBus queryBus) {
        return DefaultQueryGateway
                .builder()
                .queryBus(queryBus)
                .build();
    }

    @Singleton
    public CommandGateway commandGateway(CommandBus commandBus) {
        return DefaultCommandGateway
                .builder()
                .commandBus(commandBus)
                .dispatchInterceptors(newArrayList(new BeanValidationInterceptor<>()))
                .build();
    }
}
