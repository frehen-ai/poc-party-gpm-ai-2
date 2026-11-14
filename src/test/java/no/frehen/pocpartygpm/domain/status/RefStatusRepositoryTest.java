package no.frehen.pocpartygpm.domain.status;

import no.frehen.pocpartygpm.test.context.SpringBootIntegrationTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.simple.JdbcClient;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootIntegrationTest
class RefStatusRepositoryTest {

    @Autowired
    @Qualifier("gpm")
    JdbcClient jdbcClient;

    @Autowired
    RefStatusRepository refStatusRepository;

    @BeforeEach
    void clearRefStatusAndReferencingParties() {
        jdbcClient.sql("delete from party").update();
        jdbcClient.sql("delete from ref_status").update();
    }

    @Test
    void returns_statuses_valid_for_person_ordered_by_code() {
        insert(new RefStatus("C", "Status C", "A", "Group A", true, false, false));
        insert(new RefStatus("A", "Status A", "A", "Group A", true, true, false));
        insert(new RefStatus("B", "Status B", "B", "Group B", false, true, true));
        insert(new RefStatus("D", "Status D", "B", "Group B", true, false, true));

        assertThat(refStatusRepository.findValidForPerson())
                .extracting(RefStatus::code)
                .containsExactly("A", "C", "D");
    }

    @Test
    void maps_every_field_of_a_status() {
        insert(new RefStatus("P2", "Deceased person", "Y", "Group Y", true, false, false));
        insert(new RefStatus("P1", "Active person", "X", "Group X", true, false, true));
        insert(new RefStatus("O1", "Organisation only", "O", "Group O", false, true, false));

        assertThat(refStatusRepository.findValidForPerson())
                .first()
                .isEqualTo(new RefStatus("P1", "Active person", "X", "Group X", true, false, true));
    }

    @Test
    void returns_empty_list_when_no_status_is_valid_for_person() {
        insert(new RefStatus("O1", "Organisation only", "O", "Group O", false, true, false));
        insert(new RefStatus("M1", "Multi-customer only", "M", "Group M", false, false, true));

        assertThat(refStatusRepository.findValidForPerson()).isEmpty();
    }

    private void insert(RefStatus status) {
        jdbcClient.sql("""
                        insert into ref_status
                            (code, name, gk_code, gk_name, valid_for_person, valid_for_organisation, valid_for_multi_customer)
                        values (?, ?, ?, ?, ?, ?, ?)
                        """)
                .params(status.code(), status.name(), status.gkCode(), status.gkName(),
                        status.validForPerson(), status.validForOrganisation(), status.validForMultiCustomer())
                .update();
    }
}
