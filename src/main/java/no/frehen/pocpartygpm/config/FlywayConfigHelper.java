package no.frehen.pocpartygpm.config;

import org.flywaydb.core.api.callback.Callback;
import org.flywaydb.core.api.configuration.FluentConfiguration;
import org.flywaydb.core.api.migration.JavaMigration;
import org.springframework.boot.context.properties.PropertyMapper;
import org.springframework.boot.flyway.autoconfigure.FlywayProperties;
import org.springframework.boot.jdbc.DatabaseDriver;
import org.springframework.jdbc.support.JdbcUtils;
import org.springframework.jdbc.support.MetaDataAccessException;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;

import javax.sql.DataSource;
import java.sql.DatabaseMetaData;
import java.time.Duration;
import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * A helper class that maps properties found in {@code FlywayProperties} into a {@code FluentConfiguration}.
 * The code was taken from {@link org.springframework.boot.flyway.autoconfigure.FlywayAutoConfiguration}
 */
class FlywayConfigHelper {
    /**
     * Configure the given {@code configuration} using the given {@code properties}.
     * <p>
     * To maximize forwards- and backwards-compatibility method references are not
     * used.
     * @param configuration the configuration
     * @param properties the properties
     */
    @SuppressWarnings("removal")
      void configureProperties(FluentConfiguration configuration, FlywayProperties properties) {
        // NOTE: Using method references in the mapper methods can break
        // back-compatibility (see gh-38164)
        PropertyMapper map = PropertyMapper.get();
        String[] locations = new LocationResolver(configuration.getDataSource())
            .resolveLocations(properties.getLocations())
            .toArray(new String[0]);
        configuration.locations(locations);
        map.from(properties.getCallbackLocations())
            .when((callbackLocations) -> !ObjectUtils.isEmpty(callbackLocations))
            .to((callbackLocations) -> configuration.callbackLocations(
                new LocationResolver(configuration.getDataSource()).resolveLocations(callbackLocations)
                    .toArray(new String[0])));
        map.from(properties.isFailOnMissingLocations())
            .to((failOnMissingLocations) -> configuration.failOnMissingLocations(failOnMissingLocations));
        map.from(properties.getEncoding()).to((encoding) -> configuration.encoding(encoding));
        map.from(properties.getConnectRetries())
            .to((connectRetries) -> configuration.connectRetries(connectRetries));
        map.from(properties.getConnectRetriesInterval())
            .as(Duration::getSeconds)
            .as(Long::intValue)
            .to((connectRetriesInterval) -> configuration.connectRetriesInterval(connectRetriesInterval));
        map.from(properties.getLockRetryCount())
            .to((lockRetryCount) -> configuration.lockRetryCount(lockRetryCount));
        map.from(properties.getDefaultSchema()).to((schema) -> configuration.defaultSchema(schema));
        map.from(properties.getSchemas())
            .as(StringUtils::toStringArray)
            .to((schemas) -> configuration.schemas(schemas));
        map.from(properties.isCreateSchemas()).to((createSchemas) -> configuration.createSchemas(createSchemas));
        map.from(properties.getTable()).to((table) -> configuration.table(table));
        map.from(properties.getTablespace()).to((tablespace) -> configuration.tablespace(tablespace));
        map.from(properties.getBaselineDescription())
            .to((baselineDescription) -> configuration.baselineDescription(baselineDescription));
        map.from(properties.getBaselineVersion())
            .to((baselineVersion) -> configuration.baselineVersion(baselineVersion));
        map.from(properties.getInstalledBy()).to((installedBy) -> configuration.installedBy(installedBy));
        map.from(properties.getPlaceholders()).to((placeholders) -> configuration.placeholders(placeholders));
        map.from(properties.getPlaceholderPrefix())
            .to((placeholderPrefix) -> configuration.placeholderPrefix(placeholderPrefix));
        map.from(properties.getPlaceholderSuffix())
            .to((placeholderSuffix) -> configuration.placeholderSuffix(placeholderSuffix));
        map.from(properties.getPlaceholderSeparator())
            .to((placeHolderSeparator) -> configuration.placeholderSeparator(placeHolderSeparator));
        map.from(properties.isPlaceholderReplacement())
            .to((placeholderReplacement) -> configuration.placeholderReplacement(placeholderReplacement));
        map.from(properties.getSqlMigrationPrefix())
            .to((sqlMigrationPrefix) -> configuration.sqlMigrationPrefix(sqlMigrationPrefix));
        map.from(properties.getSqlMigrationSuffixes())
            .as(StringUtils::toStringArray)
            .to((sqlMigrationSuffixes) -> configuration.sqlMigrationSuffixes(sqlMigrationSuffixes));
        map.from(properties.getSqlMigrationSeparator())
            .to((sqlMigrationSeparator) -> configuration.sqlMigrationSeparator(sqlMigrationSeparator));
        map.from(properties.getRepeatableSqlMigrationPrefix())
            .to((repeatableSqlMigrationPrefix) -> configuration
                .repeatableSqlMigrationPrefix(repeatableSqlMigrationPrefix));
        map.from(properties.getTarget()).to((target) -> configuration.target(target));
        map.from(properties.isBaselineOnMigrate())
            .to((baselineOnMigrate) -> configuration.baselineOnMigrate(baselineOnMigrate));
        map.from(properties.isCleanDisabled()).to((cleanDisabled) -> configuration.cleanDisabled(cleanDisabled));
        map.from(properties.isGroup()).to((group) -> configuration.group(group));
        map.from(properties.isMixed()).to((mixed) -> configuration.mixed(mixed));
        map.from(properties.isOutOfOrder()).to((outOfOrder) -> configuration.outOfOrder(outOfOrder));
        map.from(properties.isSkipDefaultCallbacks())
            .to((skipDefaultCallbacks) -> configuration.skipDefaultCallbacks(skipDefaultCallbacks));
        map.from(properties.isSkipDefaultResolvers())
            .to((skipDefaultResolvers) -> configuration.skipDefaultResolvers(skipDefaultResolvers));
        map.from(properties.isValidateMigrationNaming())
            .to((validateMigrationNaming) -> configuration.validateMigrationNaming(validateMigrationNaming));
        map.from(properties.isValidateOnMigrate())
            .to((validateOnMigrate) -> configuration.validateOnMigrate(validateOnMigrate));
        map.from(properties.getInitSqls())
            .whenNot(CollectionUtils::isEmpty)
            .as((initSqls) -> StringUtils.collectionToDelimitedString(initSqls, "\n"))
            .to((initSql) -> configuration.initSql(initSql));
        map.from(properties.getScriptPlaceholderPrefix())
            .to((prefix) -> configuration.scriptPlaceholderPrefix(prefix));
        map.from(properties.getScriptPlaceholderSuffix())
            .to((suffix) -> configuration.scriptPlaceholderSuffix(suffix));
        map.from(properties.getPowershellExecutable())
            .to((powershellExecutable) -> configuration.powershellExecutable(powershellExecutable));
        configureExecuteInTransaction(configuration, properties, map);
        map.from(properties::getLoggers).to((loggers) -> configuration.loggers(loggers));
        map.from(properties::getCommunityDbSupportEnabled)
            .to((communityDbSupportEnabled) -> configuration.communityDBSupportEnabled(communityDbSupportEnabled));
        map.from(properties.getBatch()).to((batch) -> configuration.batch(batch));
        map.from(properties.getDryRunOutput()).to((dryRunOutput) -> configuration.dryRunOutput(dryRunOutput));
        map.from(properties.getErrorOverrides())
            .to((errorOverrides) -> configuration.errorOverrides(errorOverrides));
        map.from(properties.getStream()).to((stream) -> configuration.stream(stream));
        map.from(properties.getJdbcProperties())
            .whenNot(Map::isEmpty)
            .to((jdbcProperties) -> configuration.jdbcProperties(jdbcProperties));
        map.from(properties.getKerberosConfigFile())
            .to((configFile) -> configuration.kerberosConfigFile(configFile));
        map.from(properties.getOutputQueryResults())
            .to((outputQueryResults) -> configuration.outputQueryResults(outputQueryResults));
        map.from(properties.getSkipExecutingMigrations())
            .to((skipExecutingMigrations) -> configuration.skipExecutingMigrations(skipExecutingMigrations));
        map.from(properties.getIgnoreMigrationPatterns())
            .to((ignoreMigrationPatterns) -> configuration
                .ignoreMigrationPatterns(ignoreMigrationPatterns.toArray(new String[0])));
        map.from(properties.getDetectEncoding())
            .to((detectEncoding) -> configuration.detectEncoding(detectEncoding));
    }

    private void configureExecuteInTransaction(FluentConfiguration configuration, FlywayProperties properties,
                                               PropertyMapper map) {
        try {
            map.from(properties.isExecuteInTransaction()).to(configuration::executeInTransaction);
        }
        catch (NoSuchMethodError ex) {
            // Flyway < 10.x/11.x compatibility
        }
    }

    private void configureCallbacks(FluentConfiguration configuration, List<Callback> callbacks) {
        if (!callbacks.isEmpty()) {
            configuration.callbacks(callbacks.toArray(new Callback[0]));
        }
    }

    private void configureJavaMigrations(FluentConfiguration flyway, List<JavaMigration> migrations) {
        if (!migrations.isEmpty()) {
            flyway.javaMigrations(migrations.toArray(new JavaMigration[0]));
        }
    }
    private static class LocationResolver {

        private static final String VENDOR_PLACEHOLDER = "{vendor}";

        private final DataSource dataSource;

        LocationResolver(DataSource dataSource) {
            this.dataSource = dataSource;
        }

        List<String> resolveLocations(List<String> locations) {
            if (usesVendorLocation(locations)) {
                DatabaseDriver databaseDriver = getDatabaseDriver();
                return replaceVendorLocations(locations, databaseDriver);
            }
            return locations;
        }

        private List<String> replaceVendorLocations(List<String> locations, DatabaseDriver databaseDriver) {
            if (databaseDriver == DatabaseDriver.UNKNOWN) {
                return locations;
            }
            String vendor = databaseDriver.getId();
            return locations.stream().map((location) -> location.replace(VENDOR_PLACEHOLDER, vendor)).toList();
        }

        private DatabaseDriver getDatabaseDriver() {
            try {
                String url = JdbcUtils.extractDatabaseMetaData(this.dataSource, DatabaseMetaData::getURL);
                return DatabaseDriver.fromJdbcUrl(url);
            }
            catch (MetaDataAccessException ex) {
                throw new IllegalStateException(ex);
            }

        }

        private boolean usesVendorLocation(Collection<String> locations) {
            for (String location : locations) {
                if (location.contains(VENDOR_PLACEHOLDER)) {
                    return true;
                }
            }
            return false;
        }

    }
}
