package smokefree.aws.rds.secretmanager;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import io.micronaut.context.annotation.Factory;
import io.micronaut.context.annotation.Replaces;
import io.micronaut.context.annotation.Requires;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import smokefree.DataSourceFactory;

import javax.inject.Named;
import javax.inject.Singleton;
import javax.sql.DataSource;

@Slf4j
@Factory
@NoArgsConstructor
@Replaces(factory = DataSourceFactory.class)
public class RdsDataSourceFactory {
    /**
     * It returns a {@code javax.sql.DataSource} by fetching data source details from AWS Secret Manager.
     * Sooner or later, If we change AWS Secret Manager and RDS instance, corresponding details must go in {@code bootstrap/application YAML file}
     */
    @Singleton
    @Named("axon")
    @Requires(env = "aws")
    @Requires(beans = RDSSecretManager.class)
    public DataSource dataSource(RDSSecretManager rdsSecretManager) {
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
