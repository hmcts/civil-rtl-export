package uk.gov.hmcts.reform.civil.controllers;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;
import uk.gov.hmcts.reform.civil.service.ScheduledReportService;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class ReportControllerTest {

    @Mock
    private ScheduledReportService scheduledReportService;

    private ReportController reportController;

    @BeforeEach
    void setUp() {
        reportController = new ReportController(scheduledReportService);
    }

    @Test
    void triggerReportReturnsExpectedMessage() {
        LocalDateTime asOf = LocalDateTime.of(2024, 10, 22, 12, 0);
        boolean test = true;
        String serviceId = "testServiceId";

        ResponseEntity<String> responseEntity = reportController.triggerReport(asOf, test, serviceId);

        assertEquals("Processing has completed successfully for serviceId: " + serviceId, responseEntity.getBody());
        verify(scheduledReportService).generateReport(test, asOf, serviceId);
    }
}
