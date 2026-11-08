package no.frehen.pocpartygpm.config;

import com.zaxxer.hikari.HikariDataSource;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.jdbc.autoconfigure.DataSourceProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;


@Configuration
public class DbConfig {

    // --- gpm ---

    @Bean
    @Qualifier("gpm")
    @ConfigurationProperties(prefix = "gpm.datasource")
    public DataSourceProperties gpmDataSourceProperties() {
        return new DataSourceProperties();
    }

    @Bean
    @Qualifier("gpm")
    @ConfigurationProperties(prefix = "gpm.datasource.hikari")
    public HikariDataSource gpmDataSource(@Qualifier("gpm") DataSourceProperties properties) {
        return properties.initializeDataSourceBuilder()
            .type(HikariDataSource.class)
            .build();
    }

    @Bean
    @Qualifier("gpm")
    public String gpmSchemaName(@Value("${gpm.datasource.hikari.schema}") String schemaName ) {
        return schemaName;
    }


    @Bean
    @Qualifier("gpm")
    public JdbcTemplate gpmJdbcTemplate(@Qualifier("gpm") HikariDataSource dataSource) {
        return new JdbcTemplate(dataSource);
    }

    @Bean
    @Qualifier("gpm")
    public NamedParameterJdbcTemplate gpmNamedParameterJdbcTemplate(@Qualifier("gpm") HikariDataSource dataSource) {
        return new NamedParameterJdbcTemplate(dataSource);
    }

    @Bean
    @Qualifier("gpm")
    public DataSourceTransactionManager gpmTransactionManager(@Qualifier("gpm") HikariDataSource dataSource) {
        return new DataSourceTransactionManager(dataSource);
    }

    @Bean
    @Qualifier("gpm")
    public JdbcClient gpmJdbcClient(@Qualifier("gpm") HikariDataSource dataSource) {
        return JdbcClient.create(dataSource);
    }
}

