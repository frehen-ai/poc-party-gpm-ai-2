package no.frehen.pocpartygpm.test.context;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.test.context.DynamicPropertyRegistrar;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.postgresql.PostgreSQLContainer;

@TestConfiguration(proxyBeanMethods = false)
public class PostgresContainerConfiguration {

    private static final PostgreSQLContainer postgres = new PostgreSQLContainer("postgres:15.3")
            .waitingFor(Wait.forListeningPort());

    static {
        postgres.start();
    }

    /**
     * Spring @ServiceConnection only overwrites `spring.datasource.*` properties. Since the applications datasource
     * properties live in `gpm.datasource.*` it makes no sense to add the annotation to thi bean. Instead, we
     * define a {@code DynamicPropertyRegistrar} bean to extract the postgres testcontainer properties
     * and overwrite the relevant properties.
     */
    @Bean
    public PostgreSQLContainer postgreSQLContainer() {
        return postgres;
    }

    @Bean
    public DynamicPropertyRegistrar postgresPropertyRegistrar() {
        return registry -> {
            registry.add("gpm.datasource.url", postgres::getJdbcUrl);
            registry.add("gpm.datasource.username", postgres::getUsername);
            registry.add("gpm.datasource.password", postgres::getPassword);
        };
    }
}
