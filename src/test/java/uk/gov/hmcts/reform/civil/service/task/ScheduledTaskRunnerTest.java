package uk.gov.hmcts.reform.civil.service.task;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.civil.service.HousekeepingService;
import uk.gov.hmcts.reform.civil.service.ScheduledReportService;

import java.time.LocalDateTime;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class ScheduledTaskRunnerTest {

    private static final String SERVICE_ID_1 = "UT01";

    private static final boolean NOT_TEST = false;
    private static final boolean IS_TEST = true;

    private static final String AS_OF = "2025-02-07T08:17:12";
    private static final LocalDateTime DATE_TIME_AS_OF = LocalDateTime.of(2025, 2, 7, 8, 17, 12);

    private static final String TASK_NAME_UNKNOWN = "UNKNOWN_TASK";

    @Mock
    private ScheduledReportService mockScheduledReportService;

    @Mock
    private HousekeepingService mockHousekeepingService;

    private ScheduledTaskRunner scheduledTaskRunner;

    @BeforeEach
    void setUp() {
        scheduledTaskRunner = new ScheduledTaskRunner(mockScheduledReportService, mockHousekeepingService);
    }

    @Test
    void testTaskScheduledReport() {
        scheduledTaskRunner.setReportAsOf(AS_OF);
        scheduledTaskRunner.setReportTest(NOT_TEST);
        scheduledTaskRunner.setReportServiceId(SERVICE_ID_1);

        scheduledTaskRunner.run(ScheduledTaskRunner.TASK_NAME_SCHEDULED_REPORT);

        verify(mockScheduledReportService).generateReport(NOT_TEST, DATE_TIME_AS_OF, SERVICE_ID_1);
    }

    @ParameterizedTest
    @NullAndEmptySource
    void testTaskScheduledReportNoAsOf(String asOf) {
        scheduledTaskRunner.setReportAsOf(asOf);
        scheduledTaskRunner.setReportTest(NOT_TEST);
        scheduledTaskRunner.setReportServiceId(SERVICE_ID_1);

        scheduledTaskRunner.run(ScheduledTaskRunner.TASK_NAME_SCHEDULED_REPORT);

        verify(mockScheduledReportService).generateReport(NOT_TEST, null, SERVICE_ID_1);
    }

    @Test
    void testTaskScheduledReportTestMode() {
        scheduledTaskRunner.setReportAsOf(AS_OF);
        scheduledTaskRunner.setReportTest(IS_TEST);
        scheduledTaskRunner.setReportServiceId(SERVICE_ID_1);

        scheduledTaskRunner.run(ScheduledTaskRunner.TASK_NAME_SCHEDULED_REPORT);

        verify(mockScheduledReportService).generateReport(IS_TEST, DATE_TIME_AS_OF, SERVICE_ID_1);
    }

    @ParameterizedTest
    @NullAndEmptySource
    void testTaskScheduledReportNoServiceId(String serviceId) {
        scheduledTaskRunner.setReportAsOf(AS_OF);
        scheduledTaskRunner.setReportTest(NOT_TEST);
        scheduledTaskRunner.setReportServiceId(serviceId);

        scheduledTaskRunner.run(ScheduledTaskRunner.TASK_NAME_SCHEDULED_REPORT);

        verify(mockScheduledReportService).generateReport(NOT_TEST, DATE_TIME_AS_OF, null);
    }

    @Test
    void testTaskHousekeeping() {
        scheduledTaskRunner.run(ScheduledTaskRunner.TASK_NAME_HOUSEKEEPING);

        verify(mockHousekeepingService).deleteOldJudgments();
    }

    @Test
    void testTaskUnknown() {
        scheduledTaskRunner.run(TASK_NAME_UNKNOWN);

        verify(mockScheduledReportService, never()).generateReport(anyBoolean(), any(LocalDateTime.class), anyString());
        verify(mockHousekeepingService, never()).deleteOldJudgments();
    }
}
