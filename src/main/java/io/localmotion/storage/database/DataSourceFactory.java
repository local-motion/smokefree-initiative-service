package io.localmotion.storage.database;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import io.localmotion.adminjob.commands.statistics.DSProvider;
import io.micronaut.context.annotation.*;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import javax.inject.Named;
import javax.sql.DataSource;

@Slf4j
@Factory
@NoArgsConstructor
@Requires(env = {"local"})
public class DataSourceFactory {
    @Value("${mysql.jdbc.url}")
    String jdbcUrl;

    @Value("${mysql.username}")
    String username;

    @Value("${mysql.password}")
    String password;

    @Value("${mysql.driver.class.name}")
    String driverClassName;

    @Bean
    @Context
    @Named("default")
    public DataSource dataSource() {


        log.info("Local datasource is being initialized...");
        HikariConfig config = new HikariConfig();
        config.setJdbcUrl(jdbcUrl);
        config.setUsername(username);
        config.setPassword(password);
        config.setDriverClassName(driverClassName);
        HikariDataSource dataSource = new HikariDataSource(config);
        log.info("Local datasource initialized successfully");

        log.info("RDSTEST: Created local datasource {}, hashcode {}", dataSource, dataSource.hashCode());


        return dataSource;
    }

    @Bean
    @Context
    public DSProvider dsProvider() {
        log.info("RDSTEST Creating DSprovider");
        return new DSProvider(jdbcUrl, username, password, driverClassName);
    }
}
