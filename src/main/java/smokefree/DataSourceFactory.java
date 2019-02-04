package smokefree;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import io.micronaut.context.annotation.Factory;
import io.micronaut.context.annotation.Value;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import javax.inject.Named;
import javax.inject.Singleton;

@Slf4j
@Factory
@NoArgsConstructor
public class DataSourceFactory {
    @Value("${mysql.jdbc.url}")
    String jdbcUrl;

    @Value("${mysql.username}")
    String username;

    @Value("${mysql.password}")
    String password;

    @Value("${mysql.driver.class.name}")
    String driverClassName;

    @Singleton
    @Named("axon")
    public HikariDataSource dataSource() {
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

}
