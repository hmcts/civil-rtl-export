package uk.gov.hmcts.reform.civil.controllers;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;
import uk.gov.hmcts.reform.civil.exception.SaveFileException;
import uk.gov.hmcts.reform.civil.service.ScheduledReportService;

import java.io.IOException;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class ReportControllerTest {

    @Mock
    private ScheduledReportService mockScheduledReportService;

    private ReportController reportController;

    @BeforeEach
    void setUp() {
        reportController = new ReportController(mockScheduledReportService);
    }

    @Test
    void triggerReportSuccess() {
        LocalDateTime asOf = LocalDateTime.of(2024, 10, 22, 12, 0);
        String serviceId = "UT01";

        ResponseEntity<String> responseEntity = reportController.triggerReport(asOf, true, serviceId);

        assertEquals("Report has completed successfully", responseEntity.getBody());
        verify(mockScheduledReportService).generateReport(true, asOf, serviceId);
    }

    @Test
    void triggerReportHandlesException() {
        Throwable cause = new IOException("This is the cause");
        SaveFileException saveFileException = new SaveFileException(cause);
        doThrow(saveFileException).when(mockScheduledReportService).generateReport(false, null, null);

        ResponseEntity<String> responseEntity = reportController.triggerReport(null, false, null);

        assertEquals("Report failed: java.io.IOException: This is the cause", responseEntity.getBody());
        verify(mockScheduledReportService).generateReport(false, null, null);
    }

    @ParameterizedTest
    @ValueSource(strings = {"A", "AA", "AAA", "AAAAA", "aaaa", "1", "11", "111", "11111", "_"})
    void triggerReportInvalidServiceId(String serviceId) {
        ResponseEntity<String> responseEntity = reportController.triggerReport(null, false, serviceId);

        assertEquals("Report failed: Invalid Service ID", responseEntity.getBody());
        verify(mockScheduledReportService, never()).generateReport(false, null, serviceId);
    }
}
