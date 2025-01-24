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

/*
 * unit tests for JudgmentRepository class
 */

@DataJpaTest
@ActiveProfiles("itest") // Specifies the active profile for the test
@Sql(scripts = {"test.sql"})
class JudgmentRepositoryIntTest {

    @Autowired
    private JudgmentRepository judgmentRepository;

    @Test // Test for findForUpdate method
    void testFindForUpdateWithRerunAndAsOf() {
        LocalDateTime asOf = LocalDateTime.of(2024, 11, 1, 12, 0);
        List<Judgment> results = judgmentRepository.findForUpdate(true, asOf, "Sid1");

        // Assert that we get the expected result
        assertThat(results).hasSize(1); // Expecting 1 judgment
        assertThat(results.get(0).getServiceId()).isEqualTo("Sid1"); // Validate service ID
    }

    @Test // Test for findForUpdate method when judgment is not due for rerun
    void testFindForUpdateWithoutRerun() {
        LocalDateTime asOf = LocalDateTime.now().minusDays(1); // Past date (minus 1 day)
        List<Judgment> results = judgmentRepository.findForUpdate(false, asOf, "Sid2");

        // Assert that we get no results
        assertThat(results).isEmpty(); // Expecting no judgment
    }

    @Test // Test for findActiveServiceIds method
    void testFindActiveServiceIds() {
        List<String> activeServiceIds = judgmentRepository.findActiveServiceIds();

        // Assert that the active service IDs contain our test service ID
        assertThat(activeServiceIds).contains("Sid3"); // Should include Sid1
    }

    @Test // Test for findActiveServiceIds when there are no unreported judgments
    void testFindActiveServiceIdsNoUnreportedJudgments() {
        // Remove all judgments
        judgmentRepository.deleteAll();

        // Retrieve active service IDs
        List<String> activeServiceIds = judgmentRepository.findActiveServiceIds();

        // Assert that there are no active service IDs
        assertThat(activeServiceIds).isEmpty(); // Expecting an empty list
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
