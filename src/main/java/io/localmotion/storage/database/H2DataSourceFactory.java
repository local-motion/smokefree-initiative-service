package io.localmotion.storage.database;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import io.micronaut.context.annotation.*;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import javax.inject.Named;
import javax.sql.DataSource;

@Slf4j
@Factory
@NoArgsConstructor
@Requires(env = {"test"})
@Replaces(bean = DataSourceFactory.class)
public class H2DataSourceFactory {

	@Bean
	@Context
	@Named("default")
	public DataSource h2DataSource() {
		log.info("Data Source for H2 Database is configuring...");
		HikariConfig config = new HikariConfig();
		config.setJdbcUrl("jdbc:h2:mem:h2;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE");
		config.setUsername("sa");
		config.setPassword("");
		config.setDriverClassName("org.h2.Driver");
		HikariDataSource datasource = new HikariDataSource(config);
		log.info("Data Source for H2 Database is configured Successfully.");
		return datasource;
	}
}
