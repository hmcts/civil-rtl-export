package uk.gov.hmcts.reform.civil.repository;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;
import uk.gov.hmcts.reform.civil.domain.Judgment;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DataJpaTest
@ActiveProfiles("itest")
@Sql(scripts = {"test.sql"})
class JudgmentRepositoryIntTest {

    private final JudgmentRepository judgmentRepository;

    @Autowired
    public JudgmentRepositoryIntTest(JudgmentRepository judgmentRepository) {
        this.judgmentRepository = judgmentRepository;
    }

    @Test
    void testFindForUpdateWithRerunAndAsOf() {
        LocalDateTime asOf = LocalDateTime.of(2024, 11, 1, 12, 0);
        List<Judgment> results = judgmentRepository.findForUpdate(true, asOf, "Sid1");

        assertThat(results).hasSize(1);
        assertThat(results.get(0).getServiceId()).isEqualTo("Sid1");
    }

    @Test
    void testFindForUpdateWithoutRerun() {
        LocalDateTime asOf = LocalDateTime.now().minusDays(1);
        List<Judgment> results = judgmentRepository.findForUpdate(false, asOf, "Sid2");

        assertThat(results).isEmpty();
    }

    @Test
    void testFindActiveServiceIds() {
        List<String> activeServiceIds = judgmentRepository.findActiveServiceIds();

        assertThat(activeServiceIds).contains("Sid3");
    }

    @Test
    void testFindActiveServiceIdsNoUnreportedJudgments() {
        judgmentRepository.deleteAll();

        List<String> activeServiceIds = judgmentRepository.findActiveServiceIds();

        assertThat(activeServiceIds).isEmpty();
    }

    @Test
    void testDeleteJudgmentsBefore() {
        boolean doesJudgmentExistBefore = judgmentRepository.existsById(6L);
        assertTrue(doesJudgmentExistBefore, "Judgment of 91 days should exist before deletion");

        LocalDateTime dateOfDeletion = LocalDate.now().minusDays(90).atStartOfDay();

        int deletedRowCount = judgmentRepository.deleteJudgmentsBefore(dateOfDeletion);
        assertEquals(1, deletedRowCount);

        boolean doesJudgmentExistAfter = judgmentRepository.existsById(6L);
        assertFalse(doesJudgmentExistAfter, "Judgment of 91 days should not exist after deletion");
    }
}
