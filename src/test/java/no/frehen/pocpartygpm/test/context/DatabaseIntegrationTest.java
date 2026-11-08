package no.frehen.pocpartygpm.test.context;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.simple.JdbcClient;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootIntegrationTest
public class DatabaseIntegrationTest {

    @Autowired
    @Qualifier("gpm")
    JdbcClient jdbcClient;

    @Autowired
    @Qualifier("gpm")
    String gpmSchemaName;

    @Test
    void can_access_database() {
        String currentSchema = jdbcClient.sql("SELECT current_schema()")
                .query(String.class)
                .single();
        assertThat(currentSchema).isEqualTo(gpmSchemaName);
    }
}
