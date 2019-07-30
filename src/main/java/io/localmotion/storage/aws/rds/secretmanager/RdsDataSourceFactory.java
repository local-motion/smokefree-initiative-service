package io.localmotion.storage.aws.rds.secretmanager;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import io.localmotion.adminjob.commands.statistics.DSProvider;
import io.micronaut.context.annotation.*;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import io.localmotion.storage.database.DataSourceFactory;

import javax.inject.Named;
import javax.inject.Singleton;
import javax.sql.DataSource;

@Slf4j
@Factory
@NoArgsConstructor
@Replaces(bean = DataSourceFactory.class)
@Requires(env = "aws")
public class RdsDataSourceFactory {
    /**
     * It returns a {@code javax.sql.DataSource} by fetching data source details from AWS Secret Manager.
     * Sooner or later, If we change AWS Secret Manager and RDS instance, corresponding details must go in {@code bootstrap/application YAML file}
     */
    @Bean
    @Context
    @Requires(beans = RDSSecretManager.class)
    @Named("default")
    public DataSource dataSource(RDSSecretManager rdsSecretManager) {
        String pw = rdsSecretManager.getPassword();
        log.info("RDS datasource is being initialized...");
        HikariConfig config = new HikariConfig();
        config.setJdbcUrl(rdsSecretManager.getJDBCurl());
        config.setUsername(rdsSecretManager.getUsername());
        config.setPassword(pw);
        config.setDriverClassName(rdsSecretManager.getJDBCDriverClass());
        log.info("RDSTEST: Trying to create RDS datasource with password {}", pw);

        HikariDataSource dataSource = new HikariDataSource(config);
        log.info("RDS datasource initialized successfully");

        log.info("RDSTEST: Created RDS datasource {} with password {} and hashcode {}", dataSource, pw, dataSource.hashCode());


        return dataSource;
    }


    @Bean
    @Requires(beans = RDSSecretManager.class)
    @Context
    public DSProvider dsProvider(RDSSecretManager rdsSecretManager) {
        log.info("RDSTEST Creating DSprovider");
        return new DSProvider(
                rdsSecretManager.getJDBCurl(),
                rdsSecretManager.getUsername(),
                rdsSecretManager.getPassword(),
                rdsSecretManager.getJDBCDriverClass()
        );
    }
}
