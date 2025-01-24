package uk.gov.hmcts.reform.civil.service;

import jakarta.transaction.Transactional;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;
import uk.gov.hmcts.reform.civil.repository.JudgmentRepository;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@Transactional
@SpringBootTest
@ActiveProfiles("itest")
@Sql(scripts = {"classpath:uk/gov/hmcts/reform/civil/repository/HousekeepingTest.sql"})
public class HousekeepingServiceIntTest {

    private final JudgmentRepository judgmentRepository;

    private final HousekeepingService housekeepingService;

    @Value("${rtl-export.housekeeping.minimumAge}")
    private int minimumAge;

    @Autowired
    public HousekeepingServiceIntTest(JudgmentRepository judgmentRepository, HousekeepingService housekeepingService) {
        this.judgmentRepository = judgmentRepository;
        this.housekeepingService = housekeepingService;
    }

    @Test
    void testCheckForOldJudgments() {
        //Judgments set before housekeeping service is called
        //4 judgments within the sql script
        long initialRowCount = judgmentRepository.count();

        assertEquals(4L, initialRowCount, "Initial data is expected to have 4 rows");

        //Housekeeping service is run to delete old judgments
        housekeepingService.deleteOldJudgments();

        //After the service is run, 2 rows are expected to remain
        long finalRowCount = judgmentRepository.count();

        assertEquals(3L, finalRowCount, "After the service has completed, "
            + "3 judgments should remain");

        //Checking to see the status of the specific judgment rows
        //The judgments which are 90 days and 91 days old should be deleted
        //The judgment which is 89 days old and the judgment with a null reportedToRtl should remain
        boolean checkJudgment91daysDeleted = judgmentRepository.existsById(1L);
        boolean checkJudgment90daysDeleted = judgmentRepository.existsById(2L);
        boolean checkJudgment89DaysRetained = judgmentRepository.existsById(3L);
        final boolean checkNullRtlJudgmentRetained = judgmentRepository.existsById(4L);

        assertFalse(checkJudgment91daysDeleted, "Judgment 91 days old should be deleted");
        assertTrue(checkJudgment90daysDeleted, "Judgment 90 days old should remain");
        assertTrue(checkJudgment89DaysRetained, "Judgment 89 days old should remain");
        assertTrue(checkNullRtlJudgmentRetained, "Judgment with null reportedToRtl should remain");
    }

}
