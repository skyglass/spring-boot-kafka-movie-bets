package net.skycomposer.betting.bet;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.core.JdbcTemplate;

import javax.sql.DataSource;

@Configuration
public class BettingJdbcConfig {

    @Bean
    @ConfigurationProperties("spring.datasource.betting")
    public DataSourceProperties bettingDataSourceProperties() {
        return new DataSourceProperties();
    }

    @Bean
    @Primary
    @ConfigurationProperties("spring.datasource.betting.hikari")
    public DataSource bettingDataSource() {
        return bettingDataSourceProperties()
                .initializeDataSourceBuilder()
                .build();
    }

    @Bean
    public JdbcTemplate bettingJdbcTemplate(@Qualifier("bettingDataSource") DataSource dataSource) {
        return new JdbcTemplate(dataSource);
    }

}
