package uk.gov.hmcts.reform.civil.service.task;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.civil.service.ScheduledReportService;

import java.time.LocalDateTime;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class ScheduledReportTaskTest {

    private static final String AS_OF = "2025-02-07T08:17:12";
    private static final LocalDateTime DATE_TIME_AS_OF = LocalDateTime.of(2025, 2, 7, 8, 17, 12);

    private static final boolean NOT_TEST = false;
    private static final boolean IS_TEST = true;

    private static final String SERVICE_ID_1 = "UT01";

    @Mock
    private ScheduledReportService mockScheduledReportService;

    private ScheduledReportTask scheduledReportTask;

    @BeforeEach
    void setUp() {
        scheduledReportTask = new ScheduledReportTask(mockScheduledReportService, null, NOT_TEST, null);
    }

    @Test
    void testScheduledReportTask() {
        scheduledReportTask.setAsOf(AS_OF);
        scheduledReportTask.setTest(NOT_TEST);
        scheduledReportTask.setServiceId(SERVICE_ID_1);

        scheduledReportTask.run();

        verify(mockScheduledReportService).generateReport(NOT_TEST, DATE_TIME_AS_OF, SERVICE_ID_1);
    }

    @ParameterizedTest
    @NullAndEmptySource
    void testScheduledReportTaskNoAsOf(String asOf) {
        scheduledReportTask.setAsOf(asOf);
        scheduledReportTask.setTest(NOT_TEST);
        scheduledReportTask.setServiceId(SERVICE_ID_1);

        scheduledReportTask.run();

        verify(mockScheduledReportService).generateReport(NOT_TEST, null, SERVICE_ID_1);
    }

    @Test
    void testScheduledReportTaskTestMode() {
        scheduledReportTask.setAsOf(AS_OF);
        scheduledReportTask.setTest(IS_TEST);
        scheduledReportTask.setServiceId(SERVICE_ID_1);

        scheduledReportTask.run();

        verify(mockScheduledReportService).generateReport(IS_TEST, DATE_TIME_AS_OF, SERVICE_ID_1);
    }

    @ParameterizedTest
    @NullAndEmptySource
    void testScheduledReportTaskNoServiceId(String serviceId) {
        scheduledReportTask.setAsOf(AS_OF);
        scheduledReportTask.setTest(NOT_TEST);
        scheduledReportTask.setServiceId(serviceId);

        scheduledReportTask.run();

        verify(mockScheduledReportService).generateReport(NOT_TEST, DATE_TIME_AS_OF, null);
    }

    @ParameterizedTest
    @ValueSource(strings = {"A", "AA", "AAA", "AAAAA", "aaaa", "1", "11", "111", "11111", "_"})
    void testScheduledReportTaskInvalidServiceId(String serviceId) {
        scheduledReportTask.setServiceId(serviceId);

        scheduledReportTask.run();

        verify(mockScheduledReportService, never()).generateReport(anyBoolean(), any(LocalDateTime.class), anyString());
    }
}
