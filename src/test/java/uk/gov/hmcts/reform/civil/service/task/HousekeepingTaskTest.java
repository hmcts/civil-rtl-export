package uk.gov.hmcts.reform.civil.service.task;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.civil.service.HousekeepingService;

import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class HousekeepingTaskTest {

    @Mock
    private HousekeepingService mockHousekeepingService;

    private HousekeepingTask housekeepingTask;

    @BeforeEach
    public void setUp() {
        housekeepingTask = new HousekeepingTask(mockHousekeepingService);
    }

    @Test
    void testHousekeepingTask() {
        housekeepingTask.run();

        verify(mockHousekeepingService).deleteOldJudgments();
    }
}
