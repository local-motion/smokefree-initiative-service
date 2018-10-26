package smokefree;

import io.micronaut.context.annotation.Bean;
import io.micronaut.context.annotation.Factory;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.axonframework.axonserver.connector.AxonServerConfiguration;
import org.axonframework.axonserver.connector.AxonServerConnectionManager;
import org.axonframework.axonserver.connector.command.AxonServerCommandBus;
import org.axonframework.axonserver.connector.event.axon.AxonServerEventStore;
import org.axonframework.axonserver.connector.query.AxonServerQueryBus;
import org.axonframework.axonserver.connector.query.QueryPriorityCalculator;
import org.axonframework.commandhandling.CommandBus;
import org.axonframework.commandhandling.SimpleCommandBus;
import org.axonframework.commandhandling.distributed.AnnotationRoutingStrategy;
import org.axonframework.commandhandling.gateway.CommandGateway;
import org.axonframework.commandhandling.gateway.DefaultCommandGateway;
import org.axonframework.config.Configuration;
import org.axonframework.config.DefaultConfigurer;
import org.axonframework.eventhandling.EventBus;
import org.axonframework.queryhandling.*;
import org.axonframework.serialization.Serializer;
import org.axonframework.serialization.json.JacksonSerializer;
import org.axonframework.serialization.upcasting.event.NoOpEventUpcaster;
import smokefree.domain.Initiative;

@Slf4j
@Factory
@NoArgsConstructor
public class AxonFactory {
    @Bean
    public Configuration configuration(EventBus eventBus,
                                       CommandBus commandBus,
                                       QueryBus queryBus,
                                       Serializer serializer) {
        Configuration configuration = DefaultConfigurer.defaultConfiguration()
                .configureEventBus(c -> eventBus)
                .configureCommandBus(c -> commandBus)
                .configureQueryBus(c -> queryBus)
                .configureSerializer(c -> serializer)
                .configureAggregate(Initiative.class)
                .buildConfiguration();
        configuration.start();
        return configuration;
    }


    @Bean
    public Serializer jsonSerializer() {
        return JacksonSerializer.builder().build();
    }

    @Bean
    public AxonServerConfiguration axonServerConfiguration() {
        return AxonServerConfiguration.builder().build();
    }

    @Bean
    public AxonServerConnectionManager axonServerConnectionManager(AxonServerConfiguration axonServerConfiguration) {
        return new AxonServerConnectionManager(axonServerConfiguration);
    }

    @Bean
    public EventBus eventBus(AxonServerConfiguration axonServerConfiguration, AxonServerConnectionManager axonServerConnectionManager, Serializer serializer) {
        return AxonServerEventStore.builder()
                .eventSerializer(serializer)
                .snapshotSerializer(serializer)
                .configuration(axonServerConfiguration)
                .platformConnectionManager(axonServerConnectionManager)
                .upcasterChain(NoOpEventUpcaster.INSTANCE)
                .build();
    }

    @Bean
    public CommandBus commandBus(AxonServerConfiguration axonServerConfiguration, AxonServerConnectionManager axonServerConnectionManager, Serializer serializer) {
        CommandBus localSegment = SimpleCommandBus.builder().build();
        return new AxonServerCommandBus(
                axonServerConnectionManager,
                axonServerConfiguration,
                localSegment,
                serializer,
                new AnnotationRoutingStrategy());
    }

    @Bean
    public QueryBus queryBus(AxonServerConfiguration axonServerConfiguration, AxonServerConnectionManager axonServerConnectionManager, Serializer serializer) {
        SimpleQueryBus localQueryBus = SimpleQueryBus.builder().build();
        SimpleQueryUpdateEmitter queryUpdateEmitter = SimpleQueryUpdateEmitter.builder().build();
        return new AxonServerQueryBus(axonServerConnectionManager,
                axonServerConfiguration,
                queryUpdateEmitter,
                localQueryBus,
                serializer,
                serializer,
                new QueryPriorityCalculator() {
                });
    }

    @Bean
    public QueryGateway queryGateway(QueryBus queryBus) {
        return DefaultQueryGateway
                .builder()
                .queryBus(queryBus)
                .build();
    }

    @Bean
    public CommandGateway commandGateway(CommandBus commandBus) {
        return DefaultCommandGateway
                .builder()
                .commandBus(commandBus)
                .build();
    }
}
