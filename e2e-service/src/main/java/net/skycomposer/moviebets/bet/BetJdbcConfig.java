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
public class BetJdbcConfig {

    @Bean
    @ConfigurationProperties("spring.datasource.bet")
    public DataSourceProperties betDataSourceProperties() {
        return new DataSourceProperties();
    }

    @Bean
    @Primary
    @ConfigurationProperties("spring.datasource.bet.hikari")
    public DataSource betDataSource() {
        return betDataSourceProperties()
                .initializeDataSourceBuilder()
                .build();
    }

    @Bean
    public JdbcTemplate betJdbcTemplate(@Qualifier("betDataSource") DataSource dataSource) {
        return new JdbcTemplate(dataSource);
    }

}
