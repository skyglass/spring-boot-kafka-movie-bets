package net.skycomposer.moviebets.bet;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.core.JdbcTemplate;

import javax.sql.DataSource;

@Configuration
public class MarketJdbcConfig {

    @Bean
    @ConfigurationProperties("spring.datasource.market")
    public DataSourceProperties marketDataSourceProperties() {
        return new DataSourceProperties();
    }

    @Bean
    @Primary
    @ConfigurationProperties("spring.datasource.market.hikari")
    public DataSource marketDataSource() {
        return marketDataSourceProperties()
                .initializeDataSourceBuilder()
                .build();
    }

    @Bean
    public JdbcTemplate marketJdbcTemplate(@Qualifier("marketDataSource") DataSource dataSource) {
        return new JdbcTemplate(dataSource);
    }

}