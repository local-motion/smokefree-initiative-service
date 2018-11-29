package smokefree;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import io.micronaut.context.annotation.*;
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
import smokefree.aws.rds.secretmanager.RDSSecretManager;
import smokefree.domain.Initiative;
import smokefree.projection.InitiativeProjection;

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
                                       InitiativeProjection initiativeProjection) {
        // TODO: How to avoid hard-coding aggregates, event- and query handlers?
        Configurer configurer = DefaultConfigurer.defaultConfiguration()
                .configureEventBus(c -> eventBus)
                .configureCommandBus(c -> commandBus)
                .configureQueryBus(c -> queryBus)
                .configureSerializer(c -> serializer)
                .configureAggregate(Initiative.class)
                .registerQueryHandler(c -> initiativeProjection);
        configurer.eventProcessing()
                .registerEventHandler(c -> initiativeProjection);

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

    @Singleton
    @Requires(env = "local")
    @Named("axon")
    public DataSource dataSourceLocal() {
        log.info("Local datasource is being initialized...");
        HikariConfig config = new HikariConfig();
        config.setJdbcUrl("jdbc:mysql://localhost:3306/smokefree");
        config.setUsername("root");
        config.setPassword("root");
        config.setDriverClassName("com.mysql.jdbc.Driver");
        HikariDataSource dataSource = new HikariDataSource(config);
        log.info("Local datasource initialized successfully");
        return dataSource;
    }

    @Singleton
    @Requires(env = "local-docker")
    @Named("axon")
    public DataSource dataSourceLocalDocker() {
        log.info("Local datasource is being initialized...");
        HikariConfig config = new HikariConfig();
        config.setJdbcUrl("jdbc:mysql://mysql:3306/smokefree");
        config.setUsername("root");
        config.setPassword("root");
        config.setDriverClassName("com.mysql.jdbc.Driver");
        HikariDataSource dataSource = new HikariDataSource(config);
        log.info("Local datasource initialized successfully");
        return dataSource;
    }


    /**
     * It returns a {@code javax.sql.DataSource} by fetching data source details from AWS Secret Manager.
     * Sooner or later, If we change AWS Secret Manager and RDS instance, corresponding details must go in {@code bootstrap/application YAML file}
     */
    @Singleton
    @Named("axon")
    @Requires(env = "aws")
    @Requires(beans = RDSSecretManager.class)
    public DataSource awsDataSource(RDSSecretManager rdsSecretManager) {
        log.info("RDS datasource is being initialized...");
        HikariConfig config = new HikariConfig();
        config.setJdbcUrl(rdsSecretManager.getJDBCurl());
        config.setUsername(rdsSecretManager.getUsername());
        config.setPassword(rdsSecretManager.getPassword());
        config.setDriverClassName(rdsSecretManager.getJDBCDriverClass());
        HikariDataSource dataSource = new HikariDataSource(config);
        log.info("RDS datasource initialized successfully");
        return dataSource;
    }
}
