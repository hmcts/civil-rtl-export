package uk.gov.hmcts.reform.civil.repository;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;

import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DataJpaTest
@ActiveProfiles("itest")
@Sql(scripts = {"judgment_repository_housekeeping_int_test.sql"})
class JudgmentRepositoryHousekeepingIntTest {

    private final JudgmentRepository judgmentRepository;

    @Autowired
    public JudgmentRepositoryHousekeepingIntTest(JudgmentRepository judgmentRepository) {
        this.judgmentRepository = judgmentRepository;
    }

    @Test
    void testDeleteJudgmentsBefore() {
        assertTrue(judgmentRepository.existsById(1L),
                   "Judgment with reported to RTL date inside threshold should exist");
        assertTrue(judgmentRepository.existsById(2L),
                   "Judgment with reported to RTL date on threshold should exist");
        assertTrue(judgmentRepository.existsById(3L),
                   "Judgment with reported to RTL date outside threshold should exist");
        assertTrue(judgmentRepository.existsById(4L),
                   "Judgment with null reported to RTL date should exist");

        LocalDateTime dateOfDeletion = LocalDate.now().minusDays(90).atStartOfDay();
        int deletedRowCount = judgmentRepository.deleteJudgmentsBefore(dateOfDeletion);

        assertEquals(1, deletedRowCount, "Unexpected number of rows deleted");
        assertTrue(judgmentRepository.existsById(1L),
                   "Judgment with reported to RTL date inside threshold should not have been deleted");
        assertTrue(judgmentRepository.existsById(2L),
                   "Judgment with reported to RTL date on threshold should not have been deleted");
        assertFalse(judgmentRepository.existsById(3L),
                    "Judgment with reported to RTL date outside threshold should have been deleted");
        assertTrue(judgmentRepository.existsById(4L),
                   "Judgment with null reported to RTL date should not have been deleted");
    }
}
