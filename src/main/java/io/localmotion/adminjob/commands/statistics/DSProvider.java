package io.localmotion.adminjob.commands.statistics;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import javax.sql.DataSource;

@Slf4j
@AllArgsConstructor
public class DSProvider {

    String jdbcUrl;
    String username;
    String password;
    String driverClassName;



    public DataSource getDataSource() {
        log.info("RDSTEST: Local datasource is being initialized...");
        HikariConfig config = new HikariConfig();
        config.setJdbcUrl(jdbcUrl);
        config.setUsername(username);
        config.setPassword(password);
        config.setDriverClassName(driverClassName);
        log.info("RDSTEST: Trying to create datasource with password {}", password);
        HikariDataSource dataSource = new HikariDataSource(config);
        log.info("Local datasource initialized successfully");

        log.info("RDSTEST: Created datasource {}, hashcode {} and password {}", dataSource, dataSource.hashCode(), password);


        return dataSource;
    }
}
