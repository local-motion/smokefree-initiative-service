package chatbox;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import io.micronaut.context.annotation.Bean;
import io.micronaut.context.annotation.Factory;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.jpa.boot.internal.EntityManagerFactoryBuilderImpl;
import org.hibernate.jpa.boot.spi.EntityManagerFactoryBuilder;
import org.hibernate.jpa.boot.spi.PersistenceUnitDescriptor;

import javax.inject.Singleton;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.sql.DataSource;

@Slf4j
@Factory
@NoArgsConstructor
public class ChatDataSourceFactory {

    // Setting for the local environment
    private final String jdbcUrl = System.getenv("MYSQL_JDBC_URL");
    private final String username= System.getenv("MYSQL_USERNAME");
    private final String password= System.getenv("MYSQL_PASSWORD");
    private final String driverClassName= System.getenv("MYSQL_DRIVER_CLASS_NAME");

    // LogicalEnvironment selector, either 'local' or 'aws'
    private final String environments = System.getenv("MICRONAUT_ENVIRONMENTS");

    @Singleton
    public HikariDataSource dataSource() {

        // Note that although the environments property can contain multiple environment names,
        // we currently only support a single environment declaration

        if ("local".equals(environments)) {
            log.info("Local datasource is being initialized...");
            HikariConfig config = new HikariConfig();
            config.setJdbcUrl(jdbcUrl);
            config.setUsername(username);
            config.setPassword(password);
            config.setDriverClassName(driverClassName);
            HikariDataSource dataSource = new HikariDataSource(config);
            log.info("Local datasource initialized successfully");
            return dataSource;
        }
        else if ("aws".equals(environments)) {

            ChatAWSSecretManager secretManager = new ChatAWSSecretManager();
            log.info("RDS datasource is being initialized...");
            HikariConfig config = new HikariConfig();
            config.setJdbcUrl(secretManager.getJDBCurl());
            config.setUsername(secretManager.getUsername());
            config.setPassword(secretManager.getPassword());
            config.setDriverClassName(secretManager.getJDBCDriverClass());
            HikariDataSource dataSource = new HikariDataSource(config);
            log.info("RDS datasource initialized successfully");
            return dataSource;
        }
        else
            throw new IllegalArgumentException("The MICRONAUT_ENVIRONMENTS environment variable need to be set to either 'local' or 'aws' instead of '" + environments + "'");
    }

//    @Singleton
//    public EntityManagerFactory ChatEntityManagerFactory(
//            EntityManagerFactoryBuilder builder, DataSource dataSource) {
//        new EntityManagerFactoryBuilderImpl(new PersistenceUnitDescriptor() {
//        })
//        return builder.withDataSource(dataSource).build();
//    }
//
//    @Bean
//    public EntityManager chatEntityManager(EntityManagerFactory entityManagerFactory) {
//        return entityManagerFactory.createEntityManager();
//    }

}
