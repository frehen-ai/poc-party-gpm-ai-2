package no.frehen.pocpartygpm.config;

import no.frehen.pocpartygpm.constant.ProfileConstant;
import org.flywaydb.core.Flyway;
import org.flywaydb.core.api.configuration.FluentConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.flyway.autoconfigure.FlywayMigrationInitializer;
import org.springframework.boot.flyway.autoconfigure.FlywayMigrationStrategy;
import org.springframework.boot.flyway.autoconfigure.FlywayProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import javax.sql.DataSource;
import java.util.Arrays;

/**
 * Configures Flyway using configuration properties located in {@code gpm.flyway} instead of using
 * {@code spring.flyway} and autoconfiguration. This allows for multiple Flyway configurations
 * to coexist within the same Spring instance.
 */
@Configuration
public class FlywayConfig {
    private static final Logger logger = LoggerFactory.getLogger(FlywayConfig.class);
    private static final FlywayConfigHelper flywayConfigHelper = new FlywayConfigHelper();

    private void logFlywayInfo(String label, Flyway flyway) {
        var config = flyway.getConfiguration();
        var info = flyway.info();
        logger.info("Flyway [{}] — target schema(s): {}, locations: {}, current version: {}, pending: {}, applied: {}",
            label,
            Arrays.toString(config.getSchemas()),
            Arrays.toString(config.getLocations()),
            info.current() != null ? info.current().getVersion() : "none",
            info.pending().length,
            info.applied().length);

        for (var migration : info.all()) {
            logger.info("Flyway [{}] | version: {}, description: {}, state: {}, type: {}, checksum: {}",
                label,
                migration.getVersion(),
                migration.getDescription(),
                migration.getState(),
                migration.getType(),
                migration.getChecksum());
        }
    }

    // --- gpm ---

    @Bean
    @Qualifier("gpm")
    @ConfigurationProperties("gpm.flyway")
    public FlywayProperties gpmFlywayProperties() {
        return new FlywayProperties();
    }

    @Bean
    @Qualifier("gpm")
    public Flyway gpmFlyway(@Qualifier("gpm") DataSource dataSource, @Qualifier("gpm") FlywayProperties flywayProperties) {
        FluentConfiguration fluentConfiguration = new FluentConfiguration();
        flywayConfigHelper.configureProperties(fluentConfiguration, flywayProperties);
        return Flyway.configure()
            .configuration(fluentConfiguration)
            .dataSource(dataSource)
            .load();
    }

    @Bean
    @Qualifier("gpm")
    public FlywayMigrationInitializer gpmFlywayInitializer(
        @Qualifier("gpm") Flyway flyway,
        @Qualifier("gpm") FlywayMigrationStrategy strategy
    ) {
        return new FlywayMigrationInitializer(flyway, strategy);
    }

    @Profile("!local & !" + ProfileConstant.PROFILE_INTEGRATION_TEST)
    @Bean
    @Qualifier("gpm")
    public FlywayMigrationStrategy gpmFlywayMigrationStrategy() {
        return flyway -> {
            logFlywayInfo("gpm", flyway);
            flyway.validate();
        };
    }

    @Bean
    @Profile({"local", ProfileConstant.PROFILE_INTEGRATION_TEST})
    @Qualifier("gpm")
    public FlywayMigrationStrategy gpmLocalFlywayMigrationStrategy() {
        return flyway -> {
            logFlywayInfo("gpm", flyway);
            flyway.migrate();
        };
    }
}
