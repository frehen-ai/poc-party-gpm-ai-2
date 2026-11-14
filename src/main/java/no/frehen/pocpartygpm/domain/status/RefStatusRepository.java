package no.frehen.pocpartygpm.domain.status;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class RefStatusRepository {

    private final JdbcClient jdbcClient;

    public RefStatusRepository(@Qualifier("gpm") JdbcClient jdbcClient) {
        this.jdbcClient = jdbcClient;
    }

    public List<RefStatus> findValidForPerson() {
        return jdbcClient.sql("""
                select code, name, gk_code, gk_name, valid_for_person, valid_for_organisation, valid_for_multi_customer
                from ref_status
                where valid_for_person
                order by code
                """)
            .query(RefStatus.class)
            .list();
    }
}
