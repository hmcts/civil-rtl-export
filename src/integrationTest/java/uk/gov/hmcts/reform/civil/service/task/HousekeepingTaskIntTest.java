package uk.gov.hmcts.reform.civil.service.task;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.transaction.annotation.Transactional;
import uk.gov.hmcts.reform.civil.repository.JudgmentRepository;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
@ActiveProfiles("itest")
@Transactional
@Sql("housekeeping_task_int_test.sql")
class HousekeepingTaskIntTest {

    private static final String STAGE_BEFORE = "before";
    private static final String STAGE_AFTER = "after";

    private final JudgmentRepository judgmentRepository;

    private final HousekeepingTask housekeepingTask;

    @Autowired
    public HousekeepingTaskIntTest(HousekeepingTask housekeepingTask,
                                   JudgmentRepository judgmentRepository) {
        this.housekeepingTask = housekeepingTask;
        this.judgmentRepository = judgmentRepository;
    }

    @Test
    void testHousekeepingTask() {
        assertJudgmentExists(1L, STAGE_BEFORE);
        assertJudgmentExists(2L, STAGE_BEFORE);
        assertJudgmentExists(3L, STAGE_BEFORE);

        housekeepingTask.run();

        assertJudgmentExists(1L, STAGE_AFTER);
        assertJudgmentExists(2L, STAGE_AFTER);
        assertFalse(judgmentRepository.existsById(3L), "Judgment should not exist after housekeeping");
    }

    private void assertJudgmentExists(long id, String stage) {
        assertTrue(judgmentRepository.existsById(id), "Judgment should exist " + stage + " housekeeping");
    }
}
